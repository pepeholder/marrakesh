package org.example.marrakech.service;

import jakarta.transaction.Transactional;
import org.example.marrakech.dto.GameEndMessage;
import org.example.marrakech.dto.GameStatusUpdateMessage;
import org.example.marrakech.entity.Game;
import org.example.marrakech.entity.GamePlayer;
import org.example.marrakech.repository.CarpetRepository;
import org.example.marrakech.repository.GamePlayerRepository;
import org.example.marrakech.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GameCompletionService {

  private final GameRepository gameRepository;
  private final GamePlayerRepository gamePlayerRepository;
  private final CarpetRepository carpetRepository;
  private final SimpMessagingTemplate messagingTemplate;
  private final GameResultsService gameResultsService;

  @Autowired
  public GameCompletionService(GameRepository gameRepository,
                               GamePlayerRepository gamePlayerRepository,
                               CarpetRepository carpetRepository,
                               SimpMessagingTemplate messagingTemplate,
                               GameResultsService gameResultsService) {
    this.gameRepository = gameRepository;
    this.gamePlayerRepository = gamePlayerRepository;
    this.carpetRepository = carpetRepository;
    this.messagingTemplate = messagingTemplate;
    this.gameResultsService = gameResultsService;
  }

  @Transactional
  public void checkGameCompletion(Game game) {
    // Получаем всех игроков игры по game_players
    List<GamePlayer> players = gamePlayerRepository.findByGameId(game.getId());
    List<GamePlayer> activePlayers = players.stream()
        .filter(gp -> gp.getUser().isPlaying())
        .collect(Collectors.toList());

    // Если активных игроков меньше одного, ничего не делаем
    if (activePlayers.isEmpty()) {
      return;
    }

    // Если остался только один активный игрок, завершаем игру
    if (activePlayers.size() == 1) {
      finishGame(game, activePlayers.get(0).getUser().getUsername());
      return;
    }

    // Если у всех активных игроков размещено ≥ 12 ковров, завершаем игру
    boolean allFinished = activePlayers.stream()
        .allMatch(gp -> carpetRepository.countByGameAndOwner(game, gp.getUser()) >= 12);
    if (allFinished) {
      Optional<GamePlayer> winnerOpt = activePlayers.stream()
          .max(Comparator.comparingInt(GamePlayer::getCoins));
      String winnerName = winnerOpt.map(gp -> gp.getUser().getUsername()).orElse("none");
      finishGame(game, winnerName);
    }
  }

  /**
   * Завершает игру, устанавливая её статус на "finished", сохраняет в БД,
   * и рассылает WebSocket уведомление о завершении с итоговыми данными
   */
  private void finishGame(Game game, String winnerName) {
    game.setStatus("finished");
    gameRepository.save(game);
    GameStatusUpdateMessage finishUpdate = new GameStatusUpdateMessage(game.getId(), game.getStatus(), winnerName);
    messagingTemplate.convertAndSend("/topic/game/" + game.getId() + "/status", finishUpdate);
    sendGameResults(game);
  }

  private void sendGameResults(Game game) {
    GameEndMessage gameResults = gameResultsService.calculateFinalScores(game);
    messagingTemplate.convertAndSend("/topic/game/" + game.getId() + "/results", gameResults);
  }
}
