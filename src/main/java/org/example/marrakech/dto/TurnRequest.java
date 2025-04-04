package org.example.marrakech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnRequest {
  private Long gameId;
  private String movementDirection;
  private int firstX;
  private int firstY;
  private int secondX;
  private int secondY;
}
