package org.example.marrakech.service;

import org.example.marrakech.entity.Game;
import org.example.marrakech.entity.GamePlayer;
import org.example.marrakech.entity.User;
import org.example.marrakech.repository.GamePlayerRepository;
import org.example.marrakech.repository.GameRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class GameTurnService {

  private final GameRepository gameRepository;
  private final GamePlayerRepository gamePlayerRepository;

  public GameTurnService(GameRepository gameRepository, GamePlayerRepository gamePlayerRepository) {
    this.gameRepository = gameRepository;
    this.gamePlayerRepository = gamePlayerRepository;
  }

  @Transactional
  public Game switchToNextTurn(Long gameId) {
    // Получаем игру по id
    Optional<Game> optionalGame = gameRepository.findById(gameId);
    if (optionalGame.isEmpty()) {
      throw new IllegalArgumentException("Game not found with id " + gameId);
    }
    Game game = optionalGame.get();

    // Получаем список игроков, участвующих в игре
    List<GamePlayer> players = gamePlayerRepository.findByGameId(gameId);
    if (players.isEmpty()) {
      throw new IllegalArgumentException("No players in game");
    }
    // Если currentTurn не установлен, назначаем первого игрока
    User currentTurn = game.getCurrentTurn();
    if (currentTurn == null) {
      game.setCurrentTurn(players.get(0).getUser());
      return gameRepository.save(game);
    }
    // Определяем индекс текущего игрока в списке
    int currentIndex = -1;
    for (int i = 0; i < players.size(); i++) {
      if (players.get(i).getUser().getId().equals(currentTurn.getId())) {
        currentIndex = i;
        break;
      }
    }
    // Если текущий игрок не найден (что маловероятно), назначаем первого
    if (currentIndex == -1) {
      game.setCurrentTurn(players.get(0).getUser());
    } else {
      // Переключаем на следующего игрока циклически
      int nextIndex = (currentIndex + 1) % players.size();
      game.setCurrentTurn(players.get(nextIndex).getUser());
    }
    return gameRepository.save(game);
  }
}
