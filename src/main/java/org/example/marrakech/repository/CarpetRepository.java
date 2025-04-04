package org.example.marrakech.repository;

import org.example.marrakech.entity.Carpet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CarpetRepository extends JpaRepository<Carpet, Long> {
  Optional<Carpet> findByGameAndOwner(org.example.marrakech.entity.Game game, org.example.marrakech.entity.User owner);

  long countByGameAndOwner(org.example.marrakech.entity.Game game, org.example.marrakech.entity.User owner);
}
