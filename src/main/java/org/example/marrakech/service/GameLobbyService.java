package org.example.marrakech.service;

import org.example.marrakech.entity.Carpet;
import org.example.marrakech.entity.Game;
import org.example.marrakech.entity.GamePlayer;
import org.example.marrakech.entity.User;
import org.example.marrakech.repository.CarpetRepository;
import org.example.marrakech.repository.GamePlayerRepository;
import org.example.marrakech.repository.GameRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameLobbyService {

  private final GameRepository gameRepository;
  private final GamePlayerRepository gamePlayerRepository;
  private final CarpetRepository carpetRepository;

  // Задаём доступные цвета
  private static final List<String> AVAILABLE_COLORS = List.of("red", "blue", "green", "yellow");

  public GameLobbyService(GameRepository gameRepository,
                          GamePlayerRepository gamePlayerRepository,
                          CarpetRepository carpetRepository) {
    this.gameRepository = gameRepository;
    this.gamePlayerRepository = gamePlayerRepository;
    this.carpetRepository = carpetRepository;
  }

  @Transactional
  public Game joinGame(User user) {
    // Ищем игру со статусом "waiting", в которой меньше 4 игроков
    Optional<Game> optionalGame = gameRepository.findAll().stream()
        .filter(g -> "waiting".equals(g.getStatus())
            && gamePlayerRepository.countByGameId(g.getId()) < 4)
        .findFirst();

    Game selectedGame = optionalGame.orElseGet(() -> {
      // Если не найдено, создаём новую игру
      Game newGame = new Game();
      newGame.setStatus("waiting");

      // Инициализируем стартовые параметры игры
      newGame.setTurnOrder(new int[0]);
      newGame.setAssamPositionX(0);
      newGame.setAssamPositionY(0);
      newGame.setAssamDirection("up");
      newGame.setCurrentTurn(null);

      return gameRepository.save(newGame);
    });

    // Проверяем, есть ли игрок уже в игре
    if (gamePlayerRepository.existsByGameIdAndUserId(selectedGame.getId(), user.getId())) {
      return selectedGame; // Уже в игре — возвращаем её
    }

    // Определяем, какие цвета уже используются в игре
    List<GamePlayer> existingPlayers = gamePlayerRepository.findByGameId(selectedGame.getId());
    Set<String> usedColors = existingPlayers.stream()
        .map(GamePlayer::getPlayerColor)
        .collect(Collectors.toSet());
    // Доступные цвета — те, которых ещё нет
    List<String> freeColors = AVAILABLE_COLORS.stream()
        .filter(color -> !usedColors.contains(color))
        .collect(Collectors.toList());

    // Если свободных цветов нет, выбираем первый
    String assignedColor = freeColors.isEmpty() ? AVAILABLE_COLORS.get(0)
        : freeColors.get(new Random().nextInt(freeColors.size()));

    // Добавляем пользователя в игру с назначенным цветом
    GamePlayer gamePlayer = new GamePlayer(selectedGame, user, assignedColor);
    gamePlayerRepository.save(gamePlayer);

    // Создаём запись для ковра с тем же цветом
    Carpet carpet = new Carpet();
    carpet.setGame(selectedGame);
    carpet.setOwner(user);
    carpet.setColor(assignedColor);
    carpetRepository.save(carpet);

    // Если игра пустая, делаем этого игрока текущим
    if (gamePlayerRepository.countByGameId(selectedGame.getId()) == 1) {
      selectedGame.setCurrentTurn(user);
      gameRepository.save(selectedGame);
    }

    // Обновляем состояние пользователя
    user.setPlaying(true);
    user.setCurrentGame(selectedGame);

    // Если после добавления игрока общее число игроков стало 4, меняем статус игры на "in_progress"
    long playerCount = gamePlayerRepository.countByGameId(selectedGame.getId());
    if (playerCount == 4) {
      selectedGame.setStatus("in_progress");
      gameRepository.save(selectedGame);
    }

    return selectedGame;
  }
}
