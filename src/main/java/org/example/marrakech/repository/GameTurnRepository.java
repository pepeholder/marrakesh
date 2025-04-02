package org.example.marrakech.repository;

import org.example.marrakech.entity.GameTurn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameTurnRepository extends JpaRepository<GameTurn, Long> {
}
