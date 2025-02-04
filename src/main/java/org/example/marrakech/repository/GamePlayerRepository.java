package org.example.marrakech.repository;

import org.example.marrakech.entity.GamePlayer;
import org.example.marrakech.entity.GamePlayerId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GamePlayerRepository extends JpaRepository<GamePlayer, GamePlayerId> {
  List<GamePlayer> findByGameId(Long gameId);
  List<GamePlayer> findByUserId(Long userId);
}
