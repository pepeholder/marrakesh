package org.example.marrakech.repository;

import org.example.marrakech.entity.Move;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MoveRepository extends JpaRepository<Move, Long> {
  List<Move> findByGameIdOrderByTurnNumberAsc(Long gameId);
}
