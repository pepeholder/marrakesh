package org.example.marrakech.websocket;

import org.example.marrakech.entity.Game;
import org.example.marrakech.entity.GamePlayer;
import org.example.marrakech.entity.User;
import org.example.marrakech.repository.GamePlayerRepository;
import org.example.marrakech.repository.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Optional;

@Component
public class WebSocketEventListener {

  private final UserRepository userRepository;
  private final GamePlayerRepository gamePlayerRepository;
  private final SimpMessagingTemplate messagingTemplate;

  public WebSocketEventListener(UserRepository userRepository,
                                GamePlayerRepository gamePlayerRepository,
                                SimpMessagingTemplate messagingTemplate) {
    this.userRepository = userRepository;
    this.gamePlayerRepository = gamePlayerRepository;
    this.messagingTemplate = messagingTemplate;
  }

  @EventListener
  public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
    // Если у события нет аутентифицированного пользователя – выходим
    if (event.getUser() == null) {
      return;
    }

    String username = event.getUser().getName();
    Optional<User> userOpt = userRepository.findByUsername(username);
    if (userOpt.isEmpty()) {
      return;
    }

    User user = userOpt.get();
    // Если пользователь не привязан ни к одной игре, ничего делать не надо
    if (user.getCurrentGame() == null) {
      return;
    }

    Game game = user.getCurrentGame();
    if ("waiting".equalsIgnoreCase(game.getStatus())) {
      Optional<GamePlayer> gpOpt = gamePlayerRepository.findByGameIdAndUserId(game.getId(), user.getId());
      gpOpt.ifPresent(gamePlayerRepository::delete);
      // Отвязываем игру от пользователя
      user.setCurrentGame(null);
      userRepository.save(user);
      messagingTemplate.convertAndSend(
          "/topic/game/" + game.getId() + "/playerLeft",
          username + " покинул игру до её начала."
      );
    }
  }
}
