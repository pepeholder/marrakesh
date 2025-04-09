package org.example.marrakech.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
  @Column(name = "user_id")
  private Long id;

  @Column(unique = true, nullable = false)
  private String username;

  @Column(nullable = false)
  private String passwordHash;

  private boolean isPlaying;

  @ManyToOne
  @JoinColumn(name = "current_game_id")
  @JsonManagedReference
  private Game currentGame;

  public User(String username, String passwordHash) {
    this.username = username;
    this.passwordHash = passwordHash;
    this.isPlaying = false;
  }
}
