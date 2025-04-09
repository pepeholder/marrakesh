package org.example.marrakech.controller;

import org.example.marrakech.dto.CarpetPlacementAfterMoveRequest;
import org.example.marrakech.entity.Carpet;
import org.example.marrakech.entity.Game;
import org.example.marrakech.entity.User;
import org.example.marrakech.repository.CarpetRepository;
import org.example.marrakech.repository.GameRepository;
import org.example.marrakech.service.CarpetService;
import org.example.marrakech.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carpets")
public class CarpetController {

  private final CarpetRepository carpetRepository;
  private final CarpetService carpetService;
  private final UserService userService;
  private final GameRepository gameRepository;

  public CarpetController(CarpetRepository carpetRepository,
                          CarpetService carpetService,
                          UserService userService,
                          GameRepository gameRepository) {
    this.carpetRepository = carpetRepository;
    this.carpetService = carpetService;
    this.userService = userService;
    this.gameRepository = gameRepository;
  }

  @PostMapping("/placeAfterMove")
  public ResponseEntity<?> placeCarpetAfterMove(@RequestBody CarpetPlacementAfterMoveRequest request) {
    Game game = gameRepository.findById(request.getGameId())
        .orElseThrow(() -> new IllegalArgumentException("Game not found"));

    User user = userService.findByUsername(request.getUsername());
    if (user == null) {
      return ResponseEntity.badRequest().body("User not found");
    }

    if (!user.getId().equals(game.getCurrentTurn().getId())) {
      return ResponseEntity.badRequest().body("It is not your turn");
    }

    int finalX = game.getAssamPositionX();
    int finalY = game.getAssamPositionY();

    Carpet carpet = carpetRepository.findByGameAndOwner(game, user)
        .orElseThrow(() -> new IllegalArgumentException("Carpet not found for current user"));
    try {
      carpetService.placeCarpetAfterMove(
          carpet,
          finalX, finalY,
          request.getFirstX(), request.getFirstY(),
          request.getSecondX(), request.getSecondY()
      );
      return ResponseEntity.ok("Carpet placed successfully.");
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }
}
