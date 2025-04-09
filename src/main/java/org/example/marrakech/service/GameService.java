package org.example.marrakech.service;

import org.example.marrakech.entity.Game;
import org.example.marrakech.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameService {

  private final GameRepository gameRepository;

  // Определяем record для ключа (исходная позиция и направление перед выходом)
  public record PositionDirection(int x, int y, String direction) {}

  // Record для результата правила (новая позиция и новое направление)
  public record NewPositionDirection(int x, int y, String direction) {}

  // Словарь правил для переходов, если Ассам выходит за пределы поля
  private final Map<PositionDirection, NewPositionDirection> edgeRules = new HashMap<>();

  public GameService(GameRepository gameRepository) {
    this.gameRepository = gameRepository;
    initEdgeRules();
  }

  public List<Game> getAllGames() {
    return gameRepository.findAll();
  }

  public Optional<Game> getGameById(Long gameId) {
    return gameRepository.findById(gameId);
  }

  public Game createGame(Game game) {
    game.setStatus("waiting");
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

  /**
   * Перемещает Ассама на один шаг с учетом специальных правил выхода за границы.
   */
  private void moveOneStep(Game game) {
    int x = game.getAssamPositionX();
    int y = game.getAssamPositionY();
    String direction = game.getAssamDirection();

    // Рассчитываем предполагаемые новые координаты
    int newX = x;
    int newY = y;
    switch (direction) {
      case "up"    -> newY--;
      case "down"  -> newY++;
      case "left"  -> newX--;
      case "right" -> newX++;
    }

    if (isInsideBoard(newX, newY)) {
      // Если внутри поля, просто перемещаем Ассама
      game.setAssamPositionX(newX);
      game.setAssamPositionY(newY);
      return;
    }

    // Если выходит за пределы, ищем правило по текущей позиции (x,y) и направлению
    PositionDirection key = new PositionDirection(x, y, direction);
    if (edgeRules.containsKey(key)) {
      NewPositionDirection result = edgeRules.get(key);
      game.setAssamPositionX(result.x());
      game.setAssamPositionY(result.y());
      game.setAssamDirection(result.direction());
    } else {
      // Если для текущей позиции правило не задано – остаёмся на месте
      game.setAssamPositionX(x);
      game.setAssamPositionY(y);
    }
  }

  /**
   * Проверяет, что координаты (x, y) находятся внутри поля 7x7.
   */
  private boolean isInsideBoard(int x, int y) {
    return x >= 0 && x < 7 && y >= 0 && y < 7;
  }

  /**
   * Инициализирует мэп правил для выхода Ассама за границы поля.
   * Правила задаются в формате:
   * (текущая клетка, направление) → (новые координаты, новое направление).
   */
  private void initEdgeRules() {
    // Примеры для верхней границы (y == 0)
    edgeRules.put(new PositionDirection(0, 0, "up"), new NewPositionDirection(0, 0, "right"));
    edgeRules.put(new PositionDirection(0, 0, "left"), new NewPositionDirection(0, 0, "down"));
    edgeRules.put(new PositionDirection(1, 0, "up"), new NewPositionDirection(2, 0, "down"));
    edgeRules.put(new PositionDirection(2, 0, "up"), new NewPositionDirection(1, 0, "down"));
    edgeRules.put(new PositionDirection(3, 0, "up"), new NewPositionDirection(4, 0, "down"));
    edgeRules.put(new PositionDirection(4, 0, "up"), new NewPositionDirection(3, 0, "down"));
    edgeRules.put(new PositionDirection(5, 0, "up"), new NewPositionDirection(6, 0, "down"));
    edgeRules.put(new PositionDirection(6, 0, "up"), new NewPositionDirection(5, 0, "down"));

    // Правый край (x == 6)
    edgeRules.put(new PositionDirection(6, 0, "right"), new NewPositionDirection(6, 1, "left"));
    edgeRules.put(new PositionDirection(6, 1, "right"), new NewPositionDirection(6, 0, "left"));
    edgeRules.put(new PositionDirection(6, 2, "right"), new NewPositionDirection(6, 3, "left"));
    edgeRules.put(new PositionDirection(6, 3, "right"), new NewPositionDirection(6, 2, "left"));
    edgeRules.put(new PositionDirection(6, 4, "right"), new NewPositionDirection(6, 5, "left"));
    edgeRules.put(new PositionDirection(6, 5, "right"), new NewPositionDirection(6, 4, "left"));
    edgeRules.put(new PositionDirection(6, 6, "right"), new NewPositionDirection(6, 6, "up"));
    edgeRules.put(new PositionDirection(6, 6, "down"), new NewPositionDirection(6, 6, "left"));

    // Нижняя граница (y == 6)
    edgeRules.put(new PositionDirection(5, 6, "down"), new NewPositionDirection(4, 6, "up"));
    edgeRules.put(new PositionDirection(4, 6, "down"), new NewPositionDirection(5, 6, "up"));
    edgeRules.put(new PositionDirection(3, 6, "down"), new NewPositionDirection(2, 6, "up"));
    edgeRules.put(new PositionDirection(2, 6, "down"), new NewPositionDirection(3, 6, "up"));
    edgeRules.put(new PositionDirection(1, 6, "down"), new NewPositionDirection(0, 6, "up"));
    edgeRules.put(new PositionDirection(0, 6, "down"), new NewPositionDirection(1, 6, "up"));

    // Левый край (x == 0)
    edgeRules.put(new PositionDirection(0, 6, "left"), new NewPositionDirection(0, 5, "right"));
    edgeRules.put(new PositionDirection(0, 4, "left"), new NewPositionDirection(0, 3, "right"));
    edgeRules.put(new PositionDirection(0, 2, "left"), new NewPositionDirection(0, 1, "right"));

    // Можно добавить дополнительные правила, если необходимо, по аналогичной схеме
  }

  public int rollDice() {
    int[] diceOptions = {1, 2, 2, 3, 3, 4};
    Random random = new Random();
    return diceOptions[random.nextInt(diceOptions.length)];
  }
}
