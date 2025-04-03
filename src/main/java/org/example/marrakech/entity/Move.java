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
  @Column(name = "move_id")
  private Long moveId;

  @ManyToOne
  @JoinColumn(name = "game_id", nullable = false)
  private Game game;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "turn_number", nullable = false)
  private int turnNumber;

  @Column(name = "move_description", nullable = false)
  private String moveDescription;

  @Column(name = "timestamp", nullable = false)
  private LocalDateTime timestamp = LocalDateTime.now();
}
