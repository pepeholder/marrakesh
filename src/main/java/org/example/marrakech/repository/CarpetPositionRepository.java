package org.example.marrakech.repository;

import org.example.marrakech.entity.Carpet;
import org.example.marrakech.entity.CarpetPosition;
import org.example.marrakech.entity.CarpetPositionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CarpetPositionRepository extends JpaRepository<CarpetPosition, CarpetPositionId> {
  // Проверяет, существует ли ковер, занятый на заданной позиции в игре.
  @Query("SELECT cp FROM CarpetPosition cp WHERE cp.id.positionX = :x AND cp.id.positionY = :y AND cp.carpet.game.id = :gameId")
  Optional<CarpetPosition> findByGameAndPosition(Long gameId, int x, int y);
  List<CarpetPosition> findAllByCarpet(Carpet carpet);
}
