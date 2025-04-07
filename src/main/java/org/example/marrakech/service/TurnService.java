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
   * Выполняет перемещение Ассама, генерирует бросок кубика и обрабатывает оплату за наступление на чужой ковёр.
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

    // Обработка оплаты за наступление на чужой ковёр (если применимо)
    int finalX = game.getAssamPositionX();
    int finalY = game.getAssamPositionY();
    Optional<CarpetPosition> cpOpt = carpetPositionRepository.findByGameAndPosition(game.getId(), finalX, finalY);
    if (cpOpt.isPresent()) {
      Carpet carpet = cpOpt.get().getCarpet();
      User carpetOwner = carpet.getOwner();
      User currentUser = game.getCurrentTurn();
      if (!carpetOwner.getId().equals(currentUser.getId())) {
        int payment = carpetPositionRepository.findAllByCarpet(carpet).size();
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
   * Переключает текущий ход.
   * @param gameId идентификатор игры
   * @return обновлённое состояние игры с переключённым ходом
   */
  @Transactional
  public Game switchTurn(Long gameId) {
    return gameTurnService.switchToNextTurn(gameId);
  }

  private void checkGameCompletion(Game game) {
    final Game currentGame = game;
    boolean allPlayersFinished = gamePlayerRepository.findByGameId(currentGame.getId())
        .stream()
        .allMatch(gp -> carpetRepository.countByGameAndOwner(currentGame, gp.getUser()) >= 12);
    if (allPlayersFinished) {
      game.setStatus("finished");
      gameRepository.save(game);
      Optional<GamePlayer> winnerOpt = gamePlayerRepository.findByGameId(game.getId()).stream()
          .max(Comparator.comparingInt(GamePlayer::getCoins));
      String winnerName = winnerOpt.map(gp -> gp.getUser().getUsername()).orElse("none");
      GameStatusUpdateMessage finishUpdate = new GameStatusUpdateMessage(game.getId(), game.getStatus(), winnerName);
      messagingTemplate.convertAndSend("/topic/game/" + game.getId() + "/status", finishUpdate);
    } else {
      if ("waiting".equals(game.getStatus()) && gamePlayerRepository.countByGameId(game.getId()) == 4) {
        game.setStatus("in_progress");
        gameRepository.save(game);
        String currentTurnUsername = game.getCurrentTurn() != null ? game.getCurrentTurn().getUsername() : "none";
        GameStatusUpdateMessage update = new GameStatusUpdateMessage(game.getId(), game.getStatus(), currentTurnUsername);
        messagingTemplate.convertAndSend("/topic/game/" + game.getId() + "/status", update);
      }
    }
  }
}
