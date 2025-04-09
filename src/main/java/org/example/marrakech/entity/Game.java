package org.example.marrakech.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
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

  @ManyToOne
  @JoinColumn(name = "current_turn")
  private User currentTurn;

  @Column(name = "assam_position_x", nullable = false)
  @ColumnDefault("3")
  private int assamPositionX = 3;

  @Column(name = "assam_position_y", nullable = false)
  @ColumnDefault("3")
  private int assamPositionY = 3;

  @Column(name = "assam_direction", nullable = false)
  private String assamDirection = "up";

  @Column(name = "current_move_number", nullable = false)
  @ColumnDefault("1")
  private int currentMoveNumber = 1;
}
