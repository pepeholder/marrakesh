package org.example.marrakech.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerScore {
  private String username;
  private int coins;
  private int visibleCarpetTiles;
  private int totalScore;
  private int place;
}
