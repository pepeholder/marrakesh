package org.example.marrakech.service;

import org.example.marrakech.dto.TurnUpdateMessage;
import org.example.marrakech.entity.Game;
import org.example.marrakech.entity.GamePlayer;
import org.example.marrakech.entity.User;
import org.example.marrakech.repository.GamePlayerRepository;
import org.example.marrakech.repository.GameRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class GameTurnService {

  private final GameRepository gameRepository;
  private final GamePlayerRepository gamePlayerRepository;
  private final SimpMessagingTemplate messagingTemplate;

  public GameTurnService(GameRepository gameRepository, GamePlayerRepository gamePlayerRepository, SimpMessagingTemplate messagingTemplate) {
    this.gameRepository = gameRepository;
    this.gamePlayerRepository = gamePlayerRepository;
    this.messagingTemplate = messagingTemplate;
  }

  @Transactional
  public Game switchToNextTurn(Long gameId) {
    Optional<Game> optionalGame = gameRepository.findById(gameId);
    if (optionalGame.isEmpty()) {
      throw new IllegalArgumentException("Game not found with id " + gameId);
    }
    Game game = optionalGame.get();

    // Получаем игроков, отсортированных по порядку хода
    List<GamePlayer> players = gamePlayerRepository.findByGameIdOrderByTurnOrderAsc(gameId);
    if (players.isEmpty()) {
      throw new IllegalArgumentException("No players in game");
    }

    User currentTurn = game.getCurrentTurn();
    if (currentTurn == null) {
      // Если текущий игрок не задан — назначаем первого
      game.setCurrentTurn(players.get(0).getUser());
    } else {
      // Определяем индекс текущего игрока
      int currentIndex = -1;
      for (int i = 0; i < players.size(); i++) {
        if (players.get(i).getUser().getId().equals(currentTurn.getId())) {
          currentIndex = i;
          break;
        }
      }

      // Переключаем на следующего игрока по циклу
      int nextIndex = (currentIndex + 1) % players.size();
      game.setCurrentTurn(players.get(nextIndex).getUser());
    }

    game.setCurrentMoveNumber(game.getCurrentMoveNumber() + 1);

    TurnUpdateMessage message = new TurnUpdateMessage();
    message.setGameId(game.getId());
    message.setCurrentPlayerUsername(game.getCurrentTurn().getUsername());
    message.setMoveNumber(game.getCurrentMoveNumber());

    messagingTemplate.convertAndSend(
        "/topic/game/" + game.getId() + "/turn",
        message
    );

    return gameRepository.save(game);
  }
}
