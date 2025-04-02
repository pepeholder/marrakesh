package org.example.marrakech.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "moves")
@Getter
@Setter
@NoArgsConstructor
public class Move {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long moveId;

  @ManyToOne
  @JoinColumn(name = "game_id", nullable = false)
  private Game game;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private int turnNumber;

  @Column(nullable = false)
  private String moveDescription;

  @Column(nullable = false)
  private LocalDateTime timestamp = LocalDateTime.now();
}
