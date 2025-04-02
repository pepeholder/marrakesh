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

  // Сохраняем новый ход
  public Move saveMove(Move move) {
    return moveRepository.save(move);
  }

  // Получаем все ходы для заданной игры, отсортированные по номеру хода
  public List<Move> getMovesByGameId(Long gameId) {
    return moveRepository.findByGameIdOrderByTurnNumberAsc(gameId);
  }
}
