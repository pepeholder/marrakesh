package org.example.marrakech.service;

import org.example.marrakech.entity.Move;
import org.example.marrakech.repository.MoveRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MoveService {

  private final MoveRepository moveRepository;

  public MoveService(MoveRepository moveRepository) {
    this.moveRepository = moveRepository;
  }

  public Move saveMove(Move move) {
    return moveRepository.save(move);
  }

  public List<Move> getMovesForGame(Long gameId) {
    return moveRepository.findByGameIdOrderByTurnNumberAsc(gameId);
  }
}
