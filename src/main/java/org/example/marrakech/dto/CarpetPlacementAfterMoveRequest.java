package org.example.marrakech.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CarpetPlacementAfterMoveRequest {
  private Long gameId;
  private int firstX;
  private int firstY;
  private int secondX;
  private int secondY;
  private String username;
}
