package org.example.marrakech.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CarpetPositionId implements Serializable {
  private Long carpetId;

  @Column(name = "position_x")
  private int positionX;

  @Column(name = "position_y")
  private int positionY;
}
