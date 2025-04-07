package org.example.marrakech.controller;

import org.example.marrakech.dto.MoveResponse;
import org.example.marrakech.entity.Game;
import org.example.marrakech.service.TurnService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/turn")
public class TurnController {

  private final TurnService turnService;

  public TurnController(TurnService turnService) {
    this.turnService = turnService;
  }

  @PostMapping("/move")
  public ResponseEntity<MoveResponse> completeMove(@RequestParam Long gameId,
                                                   @RequestParam String movementDirection) {
    MoveResponse response = turnService.completeMove(gameId, movementDirection);
    return ResponseEntity.ok(response);
  }

  /**
   * Переключает ход после размещения ковра.
   */
  @PostMapping("/switch")
  public ResponseEntity<Game> switchTurn(@RequestParam Long gameId) {
    Game updatedGame = turnService.switchTurn(gameId);
    return ResponseEntity.ok(updatedGame);
  }
}
