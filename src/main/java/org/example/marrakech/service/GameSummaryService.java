package org.example.marrakech.service;

import org.example.marrakech.entity.Game;
import org.example.marrakech.entity.GamePlayer;
import org.example.marrakech.entity.Move;
import org.example.marrakech.repository.CarpetRepository;
import org.example.marrakech.repository.GamePlayerRepository;
import org.example.marrakech.repository.GameRepository;
import org.example.marrakech.repository.MoveRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GameSummaryService {

  private final GameRepository gameRepository;
  private final GamePlayerRepository gamePlayerRepository;
  private final MoveRepository moveRepository;
  private final CarpetRepository carpetRepository;

  public GameSummaryService(GameRepository gameRepository,
                            GamePlayerRepository gamePlayerRepository,
                            MoveRepository moveRepository,
                            CarpetRepository carpetRepository) {
    this.gameRepository = gameRepository;
    this.gamePlayerRepository = gamePlayerRepository;
    this.moveRepository = moveRepository;
    this.carpetRepository = carpetRepository;
  }

  /**
   * Собирает итоговую информацию об игре.
   *
   * @param gameId идентификатор игры
   * @return Map, содержащий:
   *         - "game": сам объект Game
   *         - "moves": список ходов (Move)
   *         - "players": список игроков (GamePlayer)
   *         - "carpetsCount": Map, где ключ – id пользователя, значение – количество размещённых ковров
   */
  public Map<String, Object> getGameSummary(Long gameId) {
    Game game = gameRepository.findById(gameId)
        .orElseThrow(() -> new IllegalArgumentException("Game not found"));

    List<Move> moves = moveRepository.findByGameIdOrderByTurnNumberAsc(gameId);
    List<GamePlayer> players = gamePlayerRepository.findByGameId(gameId);

    Map<Long, Long> carpetsCount = new HashMap<>();
    // Для каждого игрока считаем, сколько ковров он разместил в этой игре
    for (GamePlayer gp : players) {
      long count = carpetRepository.countByGameAndOwner(game, gp.getUser());
      carpetsCount.put(gp.getUser().getId(), count);
    }

    Map<String, Object> summary = new HashMap<>();
    summary.put("game", game);
    summary.put("moves", moves);
    summary.put("players", players);
    summary.put("carpetsCount", carpetsCount);

    return summary;
  }
}
