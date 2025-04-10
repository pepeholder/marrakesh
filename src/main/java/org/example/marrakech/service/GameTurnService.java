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

@Service
public class GameTurnService {

  private final GameRepository gameRepository;
  private final GamePlayerRepository gamePlayerRepository;
  private final SimpMessagingTemplate messagingTemplate;

  public GameTurnService(GameRepository gameRepository,
                         GamePlayerRepository gamePlayerRepository,
                         SimpMessagingTemplate messagingTemplate) {
    this.gameRepository = gameRepository;
    this.gamePlayerRepository = gamePlayerRepository;
    this.messagingTemplate = messagingTemplate;
  }

  @Transactional
  public Game switchToNextTurn(Long gameId) {
    Game game = getGameById(gameId);
    List<GamePlayer> players = getOrderedGamePlayers(gameId);
    updateCurrentTurn(game, players);
    game.setCurrentMoveNumber(game.getCurrentMoveNumber() + 1);
    notifyPlayersAboutTurn(game);

    return gameRepository.save(game);
  }

  private Game getGameById(Long gameId) {
    return gameRepository.findById(gameId)
        .orElseThrow(() -> new IllegalArgumentException("Не найдена игра с id " + gameId));
  }

  private List<GamePlayer> getOrderedGamePlayers(Long gameId) {
    List<GamePlayer> players = gamePlayerRepository.findByGameIdOrderByTurnOrderAsc(gameId);
    if (players.isEmpty()) {
      throw new IllegalArgumentException("Нет игроков в игре");
    }

    return players;
  }

  private void updateCurrentTurn(Game game, List<GamePlayer> players) {
    User currentTurn = game.getCurrentTurn();

    if (currentTurn == null) {
      game.setCurrentTurn(players.getFirst().getUser());
      return;
    }

    int currentIndex = findCurrentPlayerIndex(players, currentTurn);
    int nextIndex = (currentIndex + 1) % players.size();
    game.setCurrentTurn(players.get(nextIndex).getUser());
  }

  private int findCurrentPlayerIndex(List<GamePlayer> players, User currentTurn) {
    for (int i = 0; i < players.size(); i++) {
      if (players.get(i).getUser().getId().equals(currentTurn.getId())) {
        return i;
      }
    }

    throw new IllegalStateException("Игрок не найден");
  }

  private void notifyPlayersAboutTurn(Game game) {
    TurnUpdateMessage message = new TurnUpdateMessage();
    message.setGameId(game.getId());
    message.setCurrentPlayerUsername(game.getCurrentTurn().getUsername());
    message.setMoveNumber(game.getCurrentMoveNumber());

    messagingTemplate.convertAndSend(
        "/topic/game/" + game.getId() + "/turn",
        message
    );
  }
}
