package org.example.marrakech.service;

import org.example.marrakech.entity.GamePlayer;
import org.example.marrakech.repository.GamePlayerRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GamePlayerService {

  private final GamePlayerRepository gamePlayerRepository;

  public GamePlayerService(GamePlayerRepository gamePlayerRepository) {
    this.gamePlayerRepository = gamePlayerRepository;
  }

  public List<GamePlayer> getPlayersByGameId(Long gameId) {
    return gamePlayerRepository.findByGameId(gameId);
  }

  public List<GamePlayer> getGamesByUserId(Long userId) {
    return gamePlayerRepository.findByUserId(userId);
  }

  public void addGamePlayer(GamePlayer gamePlayer) {
    gamePlayerRepository.save(gamePlayer);
  }

  public void removeGamePlayer(GamePlayer gamePlayer) {
    gamePlayerRepository.delete(gamePlayer);
  }
}
