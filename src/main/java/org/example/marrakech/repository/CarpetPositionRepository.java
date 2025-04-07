package org.example.marrakech.repository;

import org.example.marrakech.entity.Carpet;
import org.example.marrakech.entity.CarpetPosition;
import org.example.marrakech.entity.CarpetPositionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CarpetPositionRepository extends JpaRepository<CarpetPosition, CarpetPositionId> {

  // Получить верхний ковер на клетке (по gameId и координатам)
  @Query("""
      SELECT cp
      FROM CarpetPosition cp
      WHERE cp.id.positionX = :x
        AND cp.id.positionY = :y
        AND cp.carpet.game.id = :gameId
      ORDER BY cp.carpet.carpetId DESC
      """)
  Optional<CarpetPosition> findTopByGameAndPosition(@Param("gameId") Long gameId,
                                                    @Param("x") int x,
                                                    @Param("y") int y);

  // Получить все ковры на клетке в порядке наложения (снизу вверх)
  @Query("""
      SELECT cp
      FROM CarpetPosition cp
      WHERE cp.id.positionX = :x
        AND cp.id.positionY = :y
        AND cp.carpet.game.id = :gameId
      ORDER BY cp.carpet.carpetId ASC
      """)
  List<CarpetPosition> findAllByGameAndPositionOrdered(@Param("gameId") Long gameId,
                                                       @Param("x") int x,
                                                       @Param("y") int y);

  // Найти ковёр по координате (любой, без гарантии порядка)
  @Query("""
      SELECT cp
      FROM CarpetPosition cp
      WHERE cp.id.positionX = :x
        AND cp.id.positionY = :y
        AND cp.carpet.game.id = :gameId
      """)
  Optional<CarpetPosition> findByGameAndPosition(@Param("gameId") Long gameId,
                                                 @Param("x") int x,
                                                 @Param("y") int y);

  // Все позиции заданного ковра
  List<CarpetPosition> findAllByCarpet(Carpet carpet);
}
