package org.example.marrakech.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String username;

  @Column(nullable = false)
  private String passwordHash;

  private boolean isPlaying;

  @ManyToOne
  @JoinColumn(name = "current_game_id")
  private Game currentGame;

  private int totalCoins;

  public User(String username, String passwordHash) {
    this.username = username;
    this.passwordHash = passwordHash;
    this.isPlaying = false;
    this.totalCoins = 0;
  }
}
