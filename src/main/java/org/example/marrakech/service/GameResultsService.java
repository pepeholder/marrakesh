package org.example.marrakech.service;

import org.example.marrakech.dto.GameEndMessage;
import org.example.marrakech.dto.PlayerScore;
import org.example.marrakech.entity.Game;
import org.example.marrakech.entity.GamePlayer;
import org.example.marrakech.repository.CarpetPositionRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameResultsService {

  private final CarpetPositionRepository carpetPositionRepository;
  private static final int BOARD_SIZE = 7;

  public GameResultsService(CarpetPositionRepository carpetPositionRepository) {
    this.carpetPositionRepository = carpetPositionRepository;
  }

  /**
   * Рассчитывает итоговые очки игры для каждого активного игрока и возвращает сообщение с результатами.
   * Итоговые очки = количество монет игрока + количество видимых частей ковра
   *
   * @param game текущее состояние игры
   * @return сообщение с итоговыми результатами игры
   */
  public GameEndMessage calculateFinalScores(Game game) {
    Map<String, Integer> visibleTileCounts = calculateVisibleTiles(game.getId());
    List<PlayerScore> playerScores = calculatePlayerScores(game, visibleTileCounts);
    assignPlayerPlaces(playerScores);

    return new GameEndMessage(game.getId(), playerScores);
  }

  /**
   * Считает, сколько видимых частей ковров расположено на поле
   *
   * @param gameId идентификатор игры
   * @return мапа, где ключ – цвет ковра, значение – количество клеток с верхним слоем этого ковра
   */
  private Map<String, Integer> calculateVisibleTiles(Long gameId) {
    Map<String, Integer> visibleTileCounts = new HashMap<>();

    for (int x = 0; x < BOARD_SIZE; x++) {
      for (int y = 0; y < BOARD_SIZE; y++) {
        carpetPositionRepository.findTopByGameAndPositionOrderByPlacementTurnDesc(gameId, x, y)
            .ifPresent(cp -> {
              String color = cp.getCarpet().getColor();
              visibleTileCounts.put(color, visibleTileCounts.getOrDefault(color, 0) + 1);
            });
      }
    }
    return visibleTileCounts;
  }

  /**
   * Вычисляет очки для каждого игрока
   *
   * @param game               объект игры
   * @param visibleTileCounts  мапа видимых частей ковров по цветам
   * @return список объектов PlayerScore, содержащих результаты для каждого игрока
   */
  private List<PlayerScore> calculatePlayerScores(Game game, Map<String, Integer> visibleTileCounts) {
    List<PlayerScore> scores = new ArrayList<>();

    for (GamePlayer player : game.getPlayers()) {
      String username = player.getUser().getUsername();
      int coins = player.getCoins();
      String color = player.getPlayerColor();
      int visibleTiles = visibleTileCounts.getOrDefault(color, 0);
      int totalScore = coins + visibleTiles;
      scores.add(new PlayerScore(username, coins, visibleTiles, totalScore, 0));
    }

    return scores;
  }

  /**
   * Сортирует список результатов и назначает места
   *
   * @param scores список результатов по игрокам
   */
  private void assignPlayerPlaces(List<PlayerScore> scores) {
    scores.sort(Comparator.comparingInt(PlayerScore::getTotalScore).reversed()
        .thenComparingInt(PlayerScore::getCoins).reversed());

    int place = 1;
    for (int i = 0; i < scores.size(); i++) {
      if (i > 0 && scores.get(i).getTotalScore() == scores.get(i - 1).getTotalScore()
          && scores.get(i).getCoins() == scores.get(i - 1).getCoins()) {
        scores.get(i).setPlace(scores.get(i - 1).getPlace());
      } else {
        scores.get(i).setPlace(place);
      }
      place++;
    }
  }
}
