package org.example.marrakech.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "game_players")
public class GamePlayer {

  @EmbeddedId
  private GamePlayerId id;

  @ManyToOne
  @MapsId("gameId")
  @JoinColumn(name = "game_id")
  private Game game;

  @ManyToOne
  @MapsId("userId")
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "player_color", nullable = false)
  private String playerColor;

  @Column(name = "coins", nullable = false)
  private int coins = 30;

  public GamePlayer() {}

  public GamePlayer(Game game, User user, String playerColor) {
    this.id = new GamePlayerId(game.getId(), user.getId());
    this.game = game;
    this.user = user;
    this.playerColor = playerColor;
  }

}
