package org.example.marrakech.service;

import org.example.marrakech.dto.GameStatusUpdateMessage;
import org.example.marrakech.entity.Carpet;
import org.example.marrakech.entity.Game;
import org.example.marrakech.entity.GamePlayer;
import org.example.marrakech.entity.User;
import org.example.marrakech.repository.CarpetRepository;
import org.example.marrakech.repository.GamePlayerRepository;
import org.example.marrakech.repository.GameRepository;
import org.example.marrakech.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameLobbyService {

  private final GameRepository gameRepository;
  private final GamePlayerRepository gamePlayerRepository;
  private final CarpetRepository carpetRepository;
  private final UserRepository userRepository;
  private final SimpMessagingTemplate messagingTemplate;

  // Доступные цвета для игроков (и их ковров)
  private static final List<String> AVAILABLE_COLORS = List.of("red", "blue", "green", "yellow");

  public GameLobbyService(GameRepository gameRepository,
                          GamePlayerRepository gamePlayerRepository,
                          CarpetRepository carpetRepository,
                          UserRepository userRepository,
                          SimpMessagingTemplate messagingTemplate) {
    this.gameRepository = gameRepository;
    this.gamePlayerRepository = gamePlayerRepository;
    this.carpetRepository = carpetRepository;
    this.userRepository = userRepository;
    this.messagingTemplate = messagingTemplate;
  }

  @Transactional
  public Game joinGame(User user) {
    Game game = getOrCreateWaitingGame();

    // Если игрок уже в игре – просто возвращаем игру
    if (gamePlayerRepository.existsByGameIdAndUserId(game.getId(), user.getId())) {
      return game;
    }

    String assignedColor = assignColorToNewPlayer(game);
    addPlayerToGame(game, user, assignedColor);
    user.setPlaying(true);
    user.setCurrentGame(game);
    userRepository.save(user);

    if (gamePlayerRepository.countByGameId(game.getId()) == 4) {
      startGame(game);
    }

    return game;
  }

  private Game getOrCreateWaitingGame() {
    Optional<Game> optionalGame = gameRepository.findAll().stream()
        .filter(g -> "waiting".equalsIgnoreCase(g.getStatus())
            && gamePlayerRepository.countByGameId(g.getId()) < 4)
        .findFirst();

    if (optionalGame.isPresent()) {
      return optionalGame.get();
    } else {
      Game newGame = new Game();
      newGame.setStatus("waiting");
      newGame.setAssamPositionX(3);
      newGame.setAssamPositionY(3);
      newGame.setAssamDirection("up");
      newGame.setCurrentTurn(null);
      newGame.setCurrentMoveNumber(1);

      return gameRepository.save(newGame);
    }
  }

  private String assignColorToNewPlayer(Game game) {
    List<GamePlayer> existingPlayers = gamePlayerRepository.findByGameId(game.getId());
    Set<String> usedColors = existingPlayers.stream()
        .map(GamePlayer::getPlayerColor)
        .collect(Collectors.toSet());

    List<String> freeColors = AVAILABLE_COLORS.stream()
        .filter(color -> !usedColors.contains(color))
        .toList();

    return freeColors.isEmpty() ? AVAILABLE_COLORS.getFirst()
        : freeColors.get(new Random().nextInt(freeColors.size()));
  }

  private void addPlayerToGame(Game game, User user, String assignedColor) {
    int currentPlayerCount = gamePlayerRepository.findByGameId(game.getId()).size();
    GamePlayer gamePlayer = new GamePlayer(game, user, assignedColor);
    gamePlayer.setTurnOrder(currentPlayerCount);
    gamePlayerRepository.save(gamePlayer);

    Carpet carpet = new Carpet();
    carpet.setGame(game);
    carpet.setOwner(user);
    carpet.setColor(assignedColor);
    carpetRepository.save(carpet);

    // Если игрок — первый, назначаем его текущим в игре
    if (gamePlayerRepository.countByGameId(game.getId()) == 1) {
      game.setCurrentTurn(user);
      gameRepository.save(game);
    }
  }

  private void startGame(Game game) {
    game.setStatus("in_progress");
    gameRepository.save(game);
    String currentTurnUsername = (game.getCurrentTurn() != null) ? game.getCurrentTurn().getUsername() : "none";
    GameStatusUpdateMessage statusUpdate = new GameStatusUpdateMessage(
        game.getId(),
        game.getStatus(),
        currentTurnUsername
    );
    messagingTemplate.convertAndSend("/topic/game/" + game.getId() + "/status", statusUpdate);
  }
}
