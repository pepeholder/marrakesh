package org.example.marrakech.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "carpet_positions")
@Getter
@Setter
@NoArgsConstructor
public class CarpetPosition {

  @EmbeddedId
  private CarpetPositionId id;

  // Связь с ковром
  @MapsId("carpetId")
  @ManyToOne
  @JoinColumn(name = "carpet_id", nullable = false)
  private Carpet carpet;
}
