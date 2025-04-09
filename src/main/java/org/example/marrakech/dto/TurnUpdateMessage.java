package org.example.marrakech.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TurnUpdateMessage {
  private Long gameId;
  private String currentPlayerUsername;
  private int moveNumber;
}
