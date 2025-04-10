package org.example.marrakech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldUpdateMessage {
  private Long gameId;
  private List<CellUpdate> updates;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CellUpdate {
    private int x;
    private int y;
    // Если null – поле пустое, иначе цвет ковра
    private String carpetColor;
  }
}
