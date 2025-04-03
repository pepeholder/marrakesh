package org.example.marrakech.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
  private String status; // waiting, in_progress, finished

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "turn_order", columnDefinition = "integer[]", nullable = false)
  private int[] turnOrder = new int[0];

  // Ссылки на currentTurn оставляем, но убираем его из JSON, чтобы разорвать цикл
  @ManyToOne
  @JoinColumn(name = "current_turn")
  @JsonBackReference  // Эта сторона не будет сериализована
  private User currentTurn;

  @Column(name = "assam_position_x", nullable = false)
  @ColumnDefault("0")
  private int assamPositionX;

  @Column(name = "assam_position_y", nullable = false)
  @ColumnDefault("0")
  private int assamPositionY;

  @Column(name = "assam_direction", nullable = false)
  private String assamDirection;
}

