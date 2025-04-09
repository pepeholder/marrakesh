package org.example.marrakech.controller;

import org.example.marrakech.dto.ErrorResponse;
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

  /**
   * Выполняет перемещение Ассама.
   * Если выбранное направление является противоположным текущему (180°), возвращает ошибку.
   */
  @PostMapping("/move")
  public ResponseEntity<?> completeMove(@RequestParam Long gameId,
                                        @RequestParam String movementDirection) {
    try {
      MoveResponse response = turnService.completeMove(gameId, movementDirection);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      // Отправляем сообщение об ошибке во фронтенд
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }
  }

  @PostMapping("/switch")
  public ResponseEntity<Game> switchTurn(@RequestParam Long gameId) {
    Game updatedGame = turnService.switchTurn(gameId);
    return ResponseEntity.ok(updatedGame);
  }
}
