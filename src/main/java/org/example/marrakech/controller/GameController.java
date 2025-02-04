package org.example.marrakech.controller;

import org.example.marrakech.entity.Game;
import org.example.marrakech.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/games")
public class GameController {
  private final GameService gameService;

  public GameController(GameService gameService) {
    this.gameService = gameService;
  }

  @GetMapping
  public ResponseEntity<List<Game>> getAllGames() {
    return ResponseEntity.ok(gameService.getAllGames());
  }

  @GetMapping("/{gameId}")
  public ResponseEntity<Game> getGameById(@PathVariable Long gameId) {
    Optional<Game> game = gameService.getGameById(gameId);
    return game.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity<Game> createGame(@RequestBody Game game) {
    return ResponseEntity.ok(gameService.createGame(game));
  }

  @PutMapping("/{gameId}")
  public ResponseEntity<Game> updateGame(@PathVariable Long gameId, @RequestBody Game updatedGame) {
    Optional<Game> existingGame = gameService.getGameById(gameId);
    if (existingGame.isPresent()) {
      Game game = existingGame.get();
      game.setStatus(updatedGame.getStatus());
      game.setTurnOrder(updatedGame.getTurnOrder());
      game.setAssamPositionX(updatedGame.getAssamPositionX());
      game.setAssamPositionY(updatedGame.getAssamPositionY());
      game.setAssamDirection(updatedGame.getAssamDirection());
      return ResponseEntity.ok(gameService.updateGame(game));
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @DeleteMapping("/{gameId}")
  public ResponseEntity<String> deleteGame(@PathVariable Long gameId) {
    gameService.deleteGame(gameId);
    return ResponseEntity.ok("Игра удалена.");
  }

  @PatchMapping("/{gameId}/status")
  public ResponseEntity<String> updateGameStatus(@PathVariable Long gameId, @RequestParam String status) {
    gameService.updateGameStatus(gameId, status);
    return ResponseEntity.ok("Статус игры обновлён.");
  }
}
