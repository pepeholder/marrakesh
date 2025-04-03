package org.example.marrakech.controller;

import org.example.marrakech.entity.Game;
import org.example.marrakech.entity.User;
import org.example.marrakech.service.GameLobbyService;
import org.example.marrakech.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lobby")
public class GameLobbyController {

  private final GameLobbyService gameLobbyService;
  private final UserService userService;

  public GameLobbyController(GameLobbyService gameLobbyService, UserService userService) {
    this.gameLobbyService = gameLobbyService;
    this.userService = userService;
  }

  @PostMapping("/join")
  public ResponseEntity<?> joinGame(@RequestParam String username) {
    User user = userService.findByUsername(username);
    if (user == null) {
      return ResponseEntity.badRequest().body("User not found");
    }
    Game game = gameLobbyService.joinGame(user);
    return ResponseEntity.ok(game);
  }
}
