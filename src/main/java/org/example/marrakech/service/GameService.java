package org.example.marrakech.service;

import org.example.marrakech.entity.Game;
import org.example.marrakech.repository.GameRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

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
}
