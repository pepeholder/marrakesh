package org.example.marrakech.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
  private String status; // Возможные значения: waiting, in_progress, finished

  @JdbcTypeCode(SqlTypes.ARRAY) // Аннотация для работы с PostgreSQL массивами
  @Column(name = "turn_order", columnDefinition = "integer[]", nullable = false)
  private int[] turnOrder = new int[0]; // Гарантируем, что массив не будет NULL

  @ManyToOne
  @JoinColumn(name = "current_turn")
  private User currentTurn;

  @Column(name = "assam_position_x", nullable = false)
  private int assamPositionX = 0;

  @Column(name = "assam_position_y", nullable = false)
  private int assamPositionY = 0;

  @Column(name = "assam_direction", nullable = false)
  private String assamDirection = "up";
}
