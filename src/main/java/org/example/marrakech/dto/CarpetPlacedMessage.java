package org.example.marrakech.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CarpetPlacedMessage {
  private int firstX;
  private int firstY;
  private int secondX;
  private int secondY;
  private String color;
  private String username;
}
