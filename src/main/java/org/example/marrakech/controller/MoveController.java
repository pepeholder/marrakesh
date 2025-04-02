package org.example.marrakech.controller;

import org.example.marrakech.entity.Move;
import org.example.marrakech.service.MoveService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/moves")
public class MoveController {

  private final MoveService moveService;

  public MoveController(MoveService moveService) {
    this.moveService = moveService;
  }

  // Эндпоинт для создания нового хода
  @PostMapping
  public ResponseEntity<Move> createMove(@RequestBody Move move) {
    Move savedMove = moveService.saveMove(move);
    return ResponseEntity.ok(savedMove);
  }

  // Эндпоинт для получения всех ходов игры по её id
  @GetMapping("/game/{gameId}")
  public ResponseEntity<List<Move>> getMovesByGameId(@PathVariable Long gameId) {
    List<Move> moves = moveService.getMovesByGameId(gameId);
    return ResponseEntity.ok(moves);
  }
}
