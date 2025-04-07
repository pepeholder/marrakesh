package org.example.marrakech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.marrakech.entity.Game;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveResponse {
  private Game game;
  private int diceRoll;
}
