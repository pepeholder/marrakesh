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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
   * Выполняет перемещение Ассама, генерирует бросок кубика, обрабатывает оплату за наступление на чужой ковёр.
   * Оплата определяется как сумма всех клеток, входящих в смежную группу (по сторонам) с конечной клеткой,
   * на которых лежит верхний ковер того же цвета.
   *
   * @param gameId идентификатор игры
   * @param movementDirection выбранное направление ("up", "down", "left", "right")
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

    // Обработка оплаты за наступление на чужой ковёр
    int finalX = game.getAssamPositionX();
    int finalY = game.getAssamPositionY();
    Optional<CarpetPosition> cpOpt = carpetPositionRepository.findTopByGameAndPositionOrderByPlacementTurnDesc(game.getId(), finalX, finalY);
    if (cpOpt.isPresent()) {
      Carpet topCarpet = cpOpt.get().getCarpet();
      User carpetOwner = topCarpet.getOwner();
      User currentUser = game.getCurrentTurn();
      // Если верхний ковер принадлежит другому игроку, то производится оплата
      if (!carpetOwner.getId().equals(currentUser.getId())) {
        int payment = carpetPositionRepository.findAllByCarpet(topCarpet).size();
        GamePlayer activeRecord = gamePlayerRepository.findByGameIdAndUserId(game.getId(), currentUser.getId())
            .orElseThrow(() -> new IllegalStateException("Active player's record not found"));
        GamePlayer ownerRecord = gamePlayerRepository.findByGameIdAndUserId(game.getId(), carpetOwner.getId())
            .orElseThrow(() -> new IllegalStateException("Owner's record not found"));
        activeRecord.setCoins(activeRecord.getCoins() - payment);
        ownerRecord.setCoins(ownerRecord.getCoins() + payment);
        if (activeRecord.getCoins() <= 0) {
          activeRecord.setCoins(0);
          currentUser.setPlaying(false);
          // Если игрок выбывает, удаляем его ковер (или применяем другую логику)
          carpetRepository.findByGameAndOwner(game, currentUser)
              .ifPresent(carpetRepository::delete);
        }
      }
    }

    return new MoveResponse(game, diceRoll);
  }

  /**
   * Переключает текущий ход.
   *
   * @param gameId идентификатор игры
   * @return обновлённое состояние игры с переключённым ходом.
   */
  @Transactional
  public Game switchTurn(Long gameId) {
    return gameTurnService.switchToNextTurn(gameId);
  }

  private void checkGameCompletion(Game game) {
    // Логика проверки завершения игры (не изменялась)
    List<GamePlayer> players = gamePlayerRepository.findByGameId(game.getId());
    List<GamePlayer> activePlayers = players.stream()
        .filter(gp -> gp.getUser().isPlaying())
        .toList();

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

  /**
   * Рекурсивный метод для подсчёта количества клеток в смежной группе, имеющих верхний ковер заданного цвета.
   * Соседство определяется по 4 направлениям (up, down, left, right).
   *
   * @param gameId идентификатор игры
   * @param x текущая координата X
   * @param y текущая координата Y
   * @param color цвет, по которому ищем группу
   * @param visited матрица посещённых клеток (размер 7x7)
   * @return количество клеток в данной группе
   */
  private int countConnectedCarpetCells(Long gameId, int x, int y, String color, boolean[][] visited) {
    if (x < 0 || x >= 7 || y < 0 || y >= 7) {
      return 0;
    }
    if (visited[x][y]) {
      return 0;
    }
    visited[x][y] = true;
    Optional<CarpetPosition> cpOpt = carpetPositionRepository.findByGameAndPosition(gameId, x, y);
    if (cpOpt.isEmpty()) {
      return 0;
    }
    CarpetPosition cp = cpOpt.get();
    // Мы будем считать только те клетки, где верхний ковер имеет нужный цвет.
    if (!cp.getCarpet().getColor().equals(color)) {
      return 0;
    }
    // Начинаем с текущей клетки (1)
    int count = 1;
    // Определяем четыре направления (up, down, left, right)
    int[] dx = { -1, 1, 0, 0 };
    int[] dy = { 0, 0, -1, 1 };
    for (int i = 0; i < 4; i++) {
      count += countConnectedCarpetCells(gameId, x + dx[i], y + dy[i], color, visited);
    }
    return count;
  }
}
