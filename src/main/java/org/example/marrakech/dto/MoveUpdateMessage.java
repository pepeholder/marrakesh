package org.example.marrakech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveUpdateMessage {
  private Long moveId;
  private Long gameId;
  private Long userId;
  private int turnNumber;
  private String moveDescription;
  private String timestamp;
}
