package org.example.marrakech.service;

import org.example.marrakech.dto.GameStatusUpdateMessage;
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

  @Transactional
  public Game completeTurn(TurnRequest request) {
    // 1. Получаем игру и перемещаем Ассама
    Game game = getGame(request.getGameId());
    int diceRoll = gameService.rollDice();
    gameService.moveAssam(game, request.getMovementDirection(), diceRoll);

    // 2. Обработка оплаты за наступление на чужой ковёр
    User currentUser = getCurrentUser(game);
    processCarpetPayment(game, currentUser);

    // 3. Размещение ковра текущим игроком
    placeCarpetForCurrentUser(game, currentUser, request);

    // 4. Смена хода
    game = switchTurn(game);

    // 5. Проверка завершения игры
    checkGameCompletion(game);

    return game;
  }

  private Game getGame(Long gameId) {
    return gameRepository.findById(gameId)
        .orElseThrow(() -> new IllegalArgumentException("Game not found"));
  }

  private User getCurrentUser(Game game) {
    User currentUser = game.getCurrentTurn();
    if (currentUser == null) {
      throw new IllegalStateException("Current turn is not set in game");
    }
    return currentUser;
  }

  private void processCarpetPayment(Game game, User currentUser) {
    int finalX = game.getAssamPositionX();
    int finalY = game.getAssamPositionY();
    Optional<CarpetPosition> cpOpt = carpetPositionRepository.findByGameAndPosition(game.getId(), finalX, finalY);
    if (cpOpt.isPresent()) {
      Carpet carpet = cpOpt.get().getCarpet();
      User carpetOwner = carpet.getOwner();
      if (!carpetOwner.getId().equals(currentUser.getId())) {
        int payment = carpetPositionRepository.findAllByCarpet(carpet).size();
        GamePlayer activePlayerRecord = gamePlayerRepository.findByGameIdAndUserId(game.getId(), currentUser.getId())
            .orElseThrow(() -> new IllegalStateException("Active player's record not found"));
        GamePlayer ownerRecord = gamePlayerRepository.findByGameIdAndUserId(game.getId(), carpetOwner.getId())
            .orElseThrow(() -> new IllegalStateException("Owner's record not found"));
        activePlayerRecord.setCoins(activePlayerRecord.getCoins() - payment);
        ownerRecord.setCoins(ownerRecord.getCoins() + payment);
        if (activePlayerRecord.getCoins() <= 0) {
          activePlayerRecord.setCoins(0);
          currentUser.setPlaying(false);
          // Можно добавить дополнительную логику деактивации ковров
        }
      }
    }
  }

  private void placeCarpetForCurrentUser(Game game, User currentUser, TurnRequest request) {
    Carpet carpet = carpetRepository.findByGameAndOwner(game, currentUser)
        .orElseThrow(() -> new IllegalArgumentException("Carpet not found for current user"));
    carpetService.placeCarpet(carpet, request.getFirstX(), request.getFirstY(),
        request.getSecondX(), request.getSecondY());
  }

  private Game switchTurn(Game game) {
    return gameTurnService.switchToNextTurn(game.getId());
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
      // Если игра только что стала заполненной (4 игрока) и статус "waiting", переключаем статус на "in_progress"
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
