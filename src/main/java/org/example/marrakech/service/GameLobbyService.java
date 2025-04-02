package org.example.marrakech.service;

import org.example.marrakech.entity.Game;
import org.example.marrakech.entity.GamePlayer;
import org.example.marrakech.entity.User;
import org.example.marrakech.repository.GamePlayerRepository;
import org.example.marrakech.repository.GameRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GameLobbyService {

  private final GameRepository gameRepository;
  private final GamePlayerRepository gamePlayerRepository;

  public GameLobbyService(GameRepository gameRepository, GamePlayerRepository gamePlayerRepository) {
    this.gameRepository = gameRepository;
    this.gamePlayerRepository = gamePlayerRepository;
  }

  @Transactional
  public Game joinGame(User user) {
    // Ищем игру со статусом "waiting", в которой меньше 4 игроков
    Optional<Game> optionalGame = gameRepository.findFirstByStatusAndPlayerCountLessThan("waiting", 4);

    Game selectedGame = optionalGame.orElseGet(() -> {
      // Если не найдено, создаём новую игру
      Game newGame = new Game();
      newGame.setStatus("waiting");

      // Инициализируем стартовые параметры игры
      newGame.setTurnOrder(new int[0]); // пустой массив вместо null
      newGame.setAssamPositionX(0);
      newGame.setAssamPositionY(0);
      newGame.setAssamDirection("up");

      return gameRepository.save(newGame);
    });

    // Проверяем, есть ли игрок уже в игре
    if (gamePlayerRepository.existsByGameIdAndUserId(selectedGame.getId(), user.getId())) {
      return selectedGame; // Уже в игре — возвращаем её
    }

    // Добавляем пользователя в игру
    GamePlayer gamePlayer = new GamePlayer(selectedGame, user, "red");
    gamePlayerRepository.save(gamePlayer);

    // Обновляем состояние пользователя
    user.setPlaying(true);
    user.setCurrentGame(selectedGame);
    // Если нужно, сохраняем изменения пользователя через userRepository
    // userRepository.save(user);

    return selectedGame;
  }
}
