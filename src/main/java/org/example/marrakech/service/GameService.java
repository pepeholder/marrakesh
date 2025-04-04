package org.example.marrakech.service;

import org.example.marrakech.entity.Game;
import org.example.marrakech.repository.GameRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class GameService {

  private final GameRepository gameRepository;

  public GameService(GameRepository gameRepository) {
    this.gameRepository = gameRepository;
  }

  public List<Game> getAllGames() {
    return gameRepository.findAll();
  }

  public Optional<Game> getGameById(Long gameId) {
    return gameRepository.findById(gameId);
  }

  public Game createGame(Game game) {
    game.setStatus("waiting"); // Устанавливаем статус по умолчанию
    return gameRepository.save(game);
  }

  public Game updateGame(Game game) {
    return gameRepository.save(game);
  }

  public void deleteGame(Long gameId) {
    gameRepository.deleteById(gameId);
  }

  public void updateGameStatus(Long gameId, String status) {
    gameRepository.findById(gameId).ifPresent(game -> {
      game.setStatus(status);
      gameRepository.save(game);
    });
  }

  public void moveAssam(Game game, String direction, int diceRoll) {
    // Нельзя повернуться в противоположную сторону
    if (isOppositeDirection(game.getAssamDirection(), direction)) {
      throw new IllegalArgumentException("Нельзя повернуться на 180 градусов");
    }

    game.setAssamDirection(direction);
    for (int i = 0; i < diceRoll; i++) {
      moveOneStep(game);
    }

    gameRepository.save(game);
  }

  private boolean isOppositeDirection(String current, String next) {
    return (current.equals("up") && next.equals("down")) ||
        (current.equals("down") && next.equals("up")) ||
        (current.equals("left") && next.equals("right")) ||
        (current.equals("right") && next.equals("left"));
  }

  private void moveOneStep(Game game) {
    int x = game.getAssamPositionX();
    int y = game.getAssamPositionY();

    switch (game.getAssamDirection()) {
      case "up" -> y--;
      case "down" -> y++;
      case "left" -> x--;
      case "right" -> x++;
    }

    // Если вышли за границу — отражаемся
    if (x < 0) {
      x = 1; game.setAssamDirection("right");
    } else if (x > 6) {
      x = 5; game.setAssamDirection("left");
    }

    if (y < 0) {
      y = 1; game.setAssamDirection("down");
    } else if (y > 6) {
      y = 5; game.setAssamDirection("up");
    }

    game.setAssamPositionX(x);
    game.setAssamPositionY(y);
  }

  public int rollDice() {
    int[] diceOptions = {1, 2, 2, 3, 3, 4};
    Random random = new Random();
    return diceOptions[random.nextInt(diceOptions.length)];
  }
}
