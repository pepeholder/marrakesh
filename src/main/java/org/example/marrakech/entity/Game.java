package org.example.marrakech.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "games")
@Getter
@Setter
@NoArgsConstructor
public class Game {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "game_id")
  private Long id;

  @Column(nullable = false)
  private String status; // waiting, in_progress, finished

  @ElementCollection
  private List<Integer> turnOrder;

  @OneToOne
  @JoinColumn(name = "current_turn")
  private User currentTurn;

  private int assamPositionX;
  private int assamPositionY;
  private String assamDirection;
}
