package org.example.marrakech.controller;

import org.example.marrakech.entity.Move;
import org.example.marrakech.service.MoveService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/moves")
public class MoveController {

  private final MoveService moveService;

  public MoveController(MoveService moveService) {
    this.moveService = moveService;
  }

  // Эндпоинт для совершения хода
  @PostMapping
  public ResponseEntity<Move> makeMove(@RequestBody Move move) {
    // Предполагается, что в теле запроса передаются корректные game и user (их id должны быть установлены)
    Move savedMove = moveService.saveMove(move);
    return ResponseEntity.ok(savedMove);
  }

  // Эндпоинт для получения истории ходов для указанной игры
  @GetMapping("/game/{gameId}")
  public ResponseEntity<List<Move>> getMovesForGame(@PathVariable Long gameId) {
    List<Move> moves = moveService.getMovesForGame(gameId);
    return ResponseEntity.ok(moves);
  }
}
