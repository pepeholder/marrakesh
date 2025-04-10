package org.example.marrakech.service;

import org.example.marrakech.dto.*;
import org.example.marrakech.entity.Carpet;
import org.example.marrakech.entity.CarpetPosition;
import org.example.marrakech.entity.Game;
import org.example.marrakech.entity.GamePlayer;
import org.example.marrakech.entity.User;
import org.example.marrakech.repository.CarpetPositionRepository;
import org.example.marrakech.repository.CarpetRepository;
import org.example.marrakech.repository.GamePlayerRepository;
import org.example.marrakech.repository.GameRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TurnService {

  private final GameRepository gameRepository;
  private final CarpetRepository carpetRepository;
  private final CarpetPositionRepository carpetPositionRepository;
  private final GameService gameService;
  private final GameTurnService gameTurnService;
  private final GamePlayerRepository gamePlayerRepository;
  private final SimpMessagingTemplate messagingTemplate;
  private final GameCompletionService gameCompletionService;

  public TurnService(GameRepository gameRepository,
                     CarpetRepository carpetRepository,
                     CarpetPositionRepository carpetPositionRepository,
                     GameService gameService,
                     GameTurnService gameTurnService,
                     GamePlayerRepository gamePlayerRepository,
                     SimpMessagingTemplate messagingTemplate,
                     GameCompletionService gameCompletionService) {
    this.gameRepository = gameRepository;
    this.carpetRepository = carpetRepository;
    this.carpetPositionRepository = carpetPositionRepository;
    this.gameService = gameService;
    this.gameTurnService = gameTurnService;
    this.gamePlayerRepository = gamePlayerRepository;
    this.messagingTemplate = messagingTemplate;
    this.gameCompletionService = gameCompletionService;
  }

  /**
   * Выполняет перемещение Ассама, генерирует бросок кубика и обрабатывает оплату за наступление
   * на чужой ковёр. Если баланс становится 0, игрок выбывает – его ковер удаляется, и
   * отправляется уведомление через WebSocket
   *
   * @param gameId идентификатор игры
   * @param movementDirection выбранное направление ("up", "down", "left", "right")
   * @return MoveResponse, содержащий обновлённое состояние игры и число, выпавшее на кубике
   */
  @Transactional
  public MoveResponse completeMove(Long gameId, String movementDirection) {
    Game game = gameRepository.findById(gameId)
        .orElseThrow(() -> new IllegalArgumentException("Игра не найдена"));

    int diceRoll = moveAssamAndSave(game, movementDirection);

    int finalX = game.getAssamPositionX();
    int finalY = game.getAssamPositionY();

    handleCarpetPaymentIfNeeded(game, finalX, finalY);

    return new MoveResponse(game, diceRoll);
  }

  private int moveAssamAndSave(Game game, String movementDirection) {
    int diceRoll = gameService.rollDice();
    gameService.moveAssam(game, movementDirection, diceRoll);
    gameRepository.save(game);
    return diceRoll;
  }

  private void handleCarpetPaymentIfNeeded(Game game, int x, int y) {
    Optional<CarpetPosition> cpOpt = carpetPositionRepository
        .findTopByGameAndPositionOrderByPlacementTurnDesc(game.getId(), x, y);

    if (cpOpt.isEmpty()) return;

    Carpet topCarpet = cpOpt.get().getCarpet();
    User carpetOwner = topCarpet.getOwner();
    User currentUser = game.getCurrentTurn();

    if (carpetOwner.getId().equals(currentUser.getId())) return;

    int payment = calculatePayment(game.getId(), x, y, topCarpet);

    GamePlayer payer = gamePlayerRepository.findByGameIdAndUserId(game.getId(), currentUser.getId())
        .orElseThrow(() -> new IllegalStateException("Active player's record not found"));
    GamePlayer receiver = gamePlayerRepository.findByGameIdAndUserId(game.getId(), carpetOwner.getId())
        .orElseThrow(() -> new IllegalStateException("Owner's record not found"));

    processPayment(game, payment, currentUser, carpetOwner, payer, receiver);
    handlePlayerEliminationIfNeeded(game, payer, currentUser);
  }

  private void processPayment(Game game, int payment, User fromUser, User toUser,
                              GamePlayer payer, GamePlayer receiver) {
    int actualPayment = Math.min(payer.getCoins(), payment);

    payer.setCoins(payer.getCoins() - actualPayment);
    receiver.setCoins(receiver.getCoins() + actualPayment);

    if (actualPayment > 0) {
      PaymentNotificationMessage msg = new PaymentNotificationMessage(
          game.getId(), fromUser.getUsername(), toUser.getUsername(), actualPayment
      );
      messagingTemplate.convertAndSend("/topic/game/" + game.getId() + "/payment", msg);
    }
  }

  private void handlePlayerEliminationIfNeeded(Game game, GamePlayer player, User user) {
    if (player.getCoins() > 0) return;

    player.setCoins(0);
    user.setPlaying(false);

    carpetRepository.findByGameAndOwner(game, user).ifPresent(carpet -> {
      carpetRepository.delete(carpet);

      FieldUpdateMessage fieldUpdate = buildFieldUpdateMessage(game, user.getId());
      messagingTemplate.convertAndSend("/topic/game/" + game.getId() + "/fieldUpdate", fieldUpdate);
    });

    PlayerEliminatedMessage eliminatedMsg = new PlayerEliminatedMessage(game.getId(), user.getUsername());
    messagingTemplate.convertAndSend("/topic/game/" + game.getId() + "/elimination", eliminatedMsg);
  }

  /**
   * Собирает обновление для игровых клеток после изменения (после выбывания игрока).
   * Для каждой клетки проверяет верхний ковер и формирует список обновлений
   */
  private FieldUpdateMessage buildFieldUpdateMessage(Game game, Long eliminatedUserId) {
    List<FieldUpdateMessage.CellUpdate> updates = new ArrayList<>();

    for (int x = 0; x < 7; x++) {
      for (int y = 0; y < 7; y++) {
        Optional<CarpetPosition> cpOpt = carpetPositionRepository.findTopByGameAndPositionOrderByPlacementTurnDesc(game.getId(), x, y);
        String carpetColor = cpOpt.map(cp -> cp.getCarpet().getColor()).orElse(null);

        if (cpOpt.isPresent() && cpOpt.get().getCarpet().getOwner().getId().equals(eliminatedUserId)) {
          updates.add(new FieldUpdateMessage.CellUpdate(x, y, carpetColor));
        }
      }
    }
    return new FieldUpdateMessage(game.getId(), updates);
  }

  /**
   * Расчитывает сумму оплаты как размер группы смежных клеток, где верхним слоем является тот же ковер.
   * Группу определяем обходом в ширину по соседним клеткам
   *
   * @param gameId идентификатор игры
   * @param startX координата x стартовой клетки
   * @param startY координата y стартовой клетки
   * @param targetCarpet целевой ковер
   * @return количество клеток в группе, покрытых верхним слоем targetCarpet
   */
  private int calculatePayment(Long gameId, int startX, int startY, Carpet targetCarpet) {
    boolean[][] visited = new boolean[7][7];
    Queue<int[]> queue = new LinkedList<>();
    queue.offer(new int[]{startX, startY});
    visited[startX][startY] = true;
    int count = 0;
    int[][] directions = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };

    while (!queue.isEmpty()) {
      int[] current = queue.poll();
      int cx = current[0];
      int cy = current[1];

      Optional<CarpetPosition> cp = carpetPositionRepository.findTopByGameAndPositionOrderByPlacementTurnDesc(gameId, cx, cy);
      if (cp.isPresent()) {
        Carpet currentCarpet = cp.get().getCarpet();

        // Если верхний ковер совпадает с targetCarpet, учитываем клетку
        if (currentCarpet.getCarpetId().equals(targetCarpet.getCarpetId())) {
          count++;
          for (int[] dir : directions) {
            int nx = cx + dir[0];
            int ny = cy + dir[1];
            if (nx >= 0 && nx < 7 && ny >= 0 && ny < 7 && !visited[nx][ny]) {
              visited[nx][ny] = true;
              queue.offer(new int[]{nx, ny});
            }
          }
        }
      }
    }
    return count;
  }

  /**
   * Переключает текущий ход, увеличивает номер хода и отправляет обновление через WebSocket
   *
   * @param gameId идентификатор игры
   * @return обновлённое состояние игры с переключённым ходом
   */
  @Transactional
  public Game switchTurn(Long gameId) {
    Game game = gameTurnService.switchToNextTurn(gameId);
    gameCompletionService.checkGameCompletion(game);

    // Проверяем, что текущее значение currentTurn не равно null
    if (game.getCurrentTurn() == null) {
      throw new IllegalStateException("Current turn is null; cannot send turn update.");
    }

    // Отправляем уведомление о смене хода, используя поле currentMoveNumber
    TurnUpdateMessage turnMsg = new TurnUpdateMessage(
        game.getId(),
        game.getCurrentTurn().getUsername(),
        game.getCurrentMoveNumber()
    );
    messagingTemplate.convertAndSend("/topic/game/" + game.getId() + "/turn", turnMsg);

    return game;
  }
}
