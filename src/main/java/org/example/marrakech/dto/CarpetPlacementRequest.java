package org.example.marrakech.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CarpetPlacementRequest {
  private Long carpetId;
  private int firstX;
  private int firstY;
  private int secondX;
  private int secondY;
}
