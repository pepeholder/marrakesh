package org.example.marrakech.repository;

import org.example.marrakech.entity.CarpetPosition;
import org.example.marrakech.entity.CarpetPositionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CarpetPositionRepository extends JpaRepository<CarpetPosition, CarpetPositionId> {

  /// Возвращает верхнюю запись на клетке
  @Query(value = "SELECT * FROM carpet_positions cp " +
      "WHERE cp.position_x = :positionX " +
      "  AND cp.position_y = :positionY " +
      "  AND cp.carpet_id IN (SELECT c.carpet_id FROM carpets c WHERE c.game_id = :gameId) " +
      "ORDER BY cp.placement_turn DESC LIMIT 1", nativeQuery = true)
  Optional<CarpetPosition> findTopByGameAndPositionOrderByPlacementTurnDesc(
      @Param("gameId") Long gameId,
      @Param("positionX") int positionX,
      @Param("positionY") int positionY);
}
