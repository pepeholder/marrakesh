package org.example.marrakech.controller;

import org.example.marrakech.entity.GamePlayer;
import org.example.marrakech.service.GamePlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/game-players")
public class GamePlayerController {
  private final GamePlayerService gamePlayerService;

  public GamePlayerController(GamePlayerService gamePlayerService) {
    this.gamePlayerService = gamePlayerService;
  }

  @GetMapping("/game/{gameId}")
  public ResponseEntity<List<GamePlayer>> getPlayersByGameId(@PathVariable Long gameId) {
    return ResponseEntity.ok(gamePlayerService.getPlayersByGameId(gameId));
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<List<GamePlayer>> getGamesByUserId(@PathVariable Long userId) {
    return ResponseEntity.ok(gamePlayerService.getGamesByUserId(userId));
  }

  @PostMapping
  public ResponseEntity<String> addGamePlayer(@RequestBody GamePlayer gamePlayer) {
    gamePlayerService.addGamePlayer(gamePlayer);
    return ResponseEntity.ok("Игрок добавлен в игру.");
  }

  @DeleteMapping
  public ResponseEntity<String> removeGamePlayer(@RequestBody GamePlayer gamePlayer) {
    gamePlayerService.removeGamePlayer(gamePlayer);
    return ResponseEntity.ok("Игрок удален из игры.");
  }
}
