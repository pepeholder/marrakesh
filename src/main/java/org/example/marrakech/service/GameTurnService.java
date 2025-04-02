package org.example.marrakech.service;

import org.example.marrakech.entity.GameTurn;
import org.example.marrakech.repository.GameTurnRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class GameTurnService {

  private final GameTurnRepository gameTurnRepository;

  public GameTurnService(GameTurnRepository gameTurnRepository) {
    this.gameTurnRepository = gameTurnRepository;
  }

  public List<GameTurn> getAllTurns() {
    return gameTurnRepository.findAll();
  }

  public Optional<GameTurn> getTurnById(Long id) {
    return gameTurnRepository.findById(id);
  }

  public GameTurn saveTurn(GameTurn gameTurn) {
    return gameTurnRepository.save(gameTurn);
  }

  public void deleteTurn(Long id) {
    gameTurnRepository.deleteById(id);
  }
}
