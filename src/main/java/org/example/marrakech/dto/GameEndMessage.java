package org.example.marrakech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GameEndMessage {
  private Long gameId;
  private List<PlayerScore> playerScores;
}
