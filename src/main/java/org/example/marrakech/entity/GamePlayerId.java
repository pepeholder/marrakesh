package org.example.marrakech.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.Objects;

@Setter
@Getter
@Embeddable
public class GamePlayerId implements Serializable {
  private Long gameId;
  private Long userId;

  public GamePlayerId() {}

  public GamePlayerId(Long gameId, Long userId) {
    this.gameId = gameId;
    this.userId = userId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    GamePlayerId that = (GamePlayerId) o;
    return Objects.equals(gameId, that.gameId) && Objects.equals(userId, that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(gameId, userId);
  }
}
