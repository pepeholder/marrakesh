package org.example.marrakech.service;

import org.example.marrakech.dto.GameStatusUpdateMessage;
import org.example.marrakech.dto.MoveResponse;
import org.example.marrakech.dto.TurnRequest;
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
import java.util.stream.Collectors;

@Service
public class TurnService {

  private final GameRepository gameRepository;
  private final CarpetRepository carpetRepository;
  private final CarpetPositionRepository carpetPositionRepository;
  private final GameService gameService;
  private final CarpetService carpetService;
  private final GameTurnService gameTurnService;
  private final GamePlayerRepository gamePlayerRepository;
  private final SimpMessagingTemplate messagingTemplate;

  public TurnService(GameRepository gameRepository,
                     CarpetRepository carpetRepository,
                     CarpetPositionRepository carpetPositionRepository,
                     GameService gameService,
                     CarpetService carpetService,
                     GameTurnService gameTurnService,
                     GamePlayerRepository gamePlayerRepository,
                     SimpMessagingTemplate messagingTemplate) {
    this.gameRepository = gameRepository;
    this.carpetRepository = carpetRepository;
    this.carpetPositionRepository = carpetPositionRepository;
    this.gameService = gameService;
    this.carpetService = carpetService;
    this.gameTurnService = gameTurnService;
    this.gamePlayerRepository = gamePlayerRepository;
    this.messagingTemplate = messagingTemplate;
  }

  /**
   * Выполняет перемещение Ассама, генерирует бросок кубика, и если Ассам заканчивает ход на
   * клетке с чужим верхним ковром, рассчитывает оплату. Оплата рассчитывается как размер группы
   * смежных клеток, покрытых верхним слоем ковра, к которому принадлежит данная клетка.
   *
   * @param gameId идентификатор игры.
   * @param movementDirection выбранное направление ("up", "down", "left", "right").
   * @return MoveResponse, содержащий обновлённое состояние игры и число, выпавшее на кубике.
   */
  @Transactional
  public MoveResponse completeMove(Long gameId, String movementDirection) {
    // Извлекаем игру
    Game game = gameRepository.findById(gameId)
        .orElseThrow(() -> new IllegalArgumentException("Game not found"));

    // Генерируем бросок кубика и перемещаем Ассама
    int diceRoll = gameService.rollDice();
    gameService.moveAssam(game, movementDirection, diceRoll);
    gameRepository.save(game);

    // Получаем конечные координаты Ассама
    int finalX = game.getAssamPositionX();
    int finalY = game.getAssamPositionY();

    // Используем метод, возвращающий только верхнюю запись на этой клетке
    Optional<CarpetPosition> cpOpt = carpetPositionRepository.findTopByGameAndPositionOrderByPlacementTurnDesc(game.getId(), finalX, finalY);
    if (cpOpt.isPresent()) {
      Carpet topCarpet = cpOpt.get().getCarpet();
      User carpetOwner = topCarpet.getOwner();
      User currentUser = game.getCurrentTurn();
      // Если верхний ковер принадлежит противнику, рассчитываем платёж
      if (!carpetOwner.getId().equals(currentUser.getId())) {
        int payment = calculatePayment(game.getId(), finalX, finalY, topCarpet);
        GamePlayer activeRecord = gamePlayerRepository.findByGameIdAndUserId(game.getId(), currentUser.getId())
            .orElseThrow(() -> new IllegalStateException("Active player's record not found"));
        GamePlayer ownerRecord = gamePlayerRepository.findByGameIdAndUserId(game.getId(), carpetOwner.getId())
            .orElseThrow(() -> new IllegalStateException("Owner's record not found"));
        activeRecord.setCoins(activeRecord.getCoins() - payment);
        ownerRecord.setCoins(ownerRecord.getCoins() + payment);
        if (activeRecord.getCoins() <= 0) {
          activeRecord.setCoins(0);
          currentUser.setPlaying(false);
          // Если игрок выбывает, его ковер удаляется
          carpetRepository.findByGameAndOwner(game, currentUser)
              .ifPresent(carpetRepository::delete);
        }
      }
    }

    return new MoveResponse(game, diceRoll);
  }

  /**
   * Расчитывает сумму оплаты как размер группы смежных клеток, на которых верхним слоем
   * является тот же ковер.
   *
   * @param gameId идентификатор игры.
   * @param startX начальная клетка X.
   * @param startY начальная клетка Y.
   * @param targetCarpet целевой ковер (верхний ковер на клетке).
   * @return размер группы клеток с ковром targetCarpet.
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

      // Получаем верхний ковер для клетки (через findByGameAndPosition)
      Optional<CarpetPosition> cp = carpetPositionRepository.findByGameAndPosition(gameId, cx, cy);
      if (cp.isPresent()) {
        Carpet currentCarpet = cp.get().getCarpet();
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
   * Переключает текущий ход, увеличивает номер хода и отправляет уведомление через WebSocket.
   *
   * @param gameId идентификатор игры.
   * @return обновлённое состояние игры с переключённым ходом.
   */
  @Transactional
  public Game switchTurn(Long gameId) {
    Game game = gameTurnService.switchToNextTurn(gameId);
    checkGameCompletion(game);
    return game;
  }

  /**
   * Проверяет, завершилась ли игра. Игра завершается, если:
   * - Остался только один активный игрок, или
   * - У всех активных игроков размещено не менее 12 ковров.
   *
   * При завершении игры отправляется уведомление через WebSocket.
   *
   * @param game игра, которую нужно проверить.
   */
  private void checkGameCompletion(Game game) {
    List<GamePlayer> players = gamePlayerRepository.findByGameId(game.getId());
    List<GamePlayer> activePlayers = players.stream()
        .filter(gp -> gp.getUser().isPlaying())
        .collect(Collectors.toList());

    if (activePlayers.size() == 1) {
      game.setStatus("finished");
      gameRepository.save(game);
      String winnerName = activePlayers.get(0).getUser().getUsername();
      GameStatusUpdateMessage finishUpdate = new GameStatusUpdateMessage(game.getId(), game.getStatus(), winnerName);
      messagingTemplate.convertAndSend("/topic/game/" + game.getId() + "/status", finishUpdate);
      return;
    }

    boolean allPlayersFinished = activePlayers.stream()
        .allMatch(gp -> carpetRepository.countByGameAndOwner(game, gp.getUser()) >= 12);
    if (allPlayersFinished) {
      game.setStatus("finished");
      gameRepository.save(game);
      Optional<GamePlayer> winnerOpt = activePlayers.stream()
          .max(Comparator.comparingInt(GamePlayer::getCoins));
      String winnerName = winnerOpt.map(gp -> gp.getUser().getUsername()).orElse("none");
      GameStatusUpdateMessage finishUpdate = new GameStatusUpdateMessage(game.getId(), game.getStatus(), winnerName);
      messagingTemplate.convertAndSend("/topic/game/" + game.getId() + "/status", finishUpdate);
    }
  }
}
