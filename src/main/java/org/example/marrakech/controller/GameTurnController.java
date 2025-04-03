package org.example.marrakech.controller;

import org.example.marrakech.entity.Game;
import org.example.marrakech.service.GameTurnService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game-turn")
public class GameTurnController {

  private final GameTurnService gameTurnService;

  public GameTurnController(GameTurnService gameTurnService) {
    this.gameTurnService = gameTurnService;
  }

  // Эндпоинт для переключения текущего игрока в игре с указанным id
  @PostMapping("/{gameId}/next")
  public ResponseEntity<?> nextTurn(@PathVariable Long gameId) {
    try {
      Game updatedGame = gameTurnService.switchToNextTurn(gameId);
      return ResponseEntity.ok(updatedGame);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Error switching turn: " + e.getMessage());
    }
  }
}
