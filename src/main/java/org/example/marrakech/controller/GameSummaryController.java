package org.example.marrakech.controller;

import org.example.marrakech.service.GameSummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/game-summary")
public class GameSummaryController {

  private final GameSummaryService gameSummaryService;

  public GameSummaryController(GameSummaryService gameSummaryService) {
    this.gameSummaryService = gameSummaryService;
  }

  @GetMapping("/{gameId}")
  public ResponseEntity<Map<String, Object>> getGameSummary(@PathVariable Long gameId) {
    Map<String, Object> summary = gameSummaryService.getGameSummary(gameId);
    return ResponseEntity.ok(summary);
  }
}
