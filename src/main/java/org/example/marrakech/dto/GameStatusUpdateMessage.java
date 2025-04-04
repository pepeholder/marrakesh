package org.example.marrakech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameStatusUpdateMessage {
  private Long gameId;
  private String status;
  private String currentTurnUsername;
}
