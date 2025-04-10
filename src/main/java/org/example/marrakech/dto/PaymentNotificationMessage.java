package org.example.marrakech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentNotificationMessage {
  private Long gameId;
  private String payerUsername;
  private String payeeUsername;
  private int amount;
}
