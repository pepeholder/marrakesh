package org.example.marrakech.repository;

import org.example.marrakech.entity.Move;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MoveRepository extends JpaRepository<Move, Long> {
  // Получаем список ходов по id игры, отсортированных по номеру хода
  List<Move> findByGameIdOrderByTurnNumberAsc(Long gameId);
}
