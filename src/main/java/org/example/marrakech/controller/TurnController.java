package org.example.marrakech.controller;

import org.example.marrakech.dto.TurnRequest;
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

  @PostMapping("/complete")
  public ResponseEntity<Game> completeTurn(@RequestBody TurnRequest request) {
    Game updatedGame = turnService.completeTurn(request);
    return ResponseEntity.ok(updatedGame);
  }
}
