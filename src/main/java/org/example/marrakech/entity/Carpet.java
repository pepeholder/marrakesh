package org.example.marrakech.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "carpets")
@Getter
@Setter
@NoArgsConstructor
public class Carpet {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long carpetId;

  @ManyToOne
  @JoinColumn(name = "game_id", nullable = false)
  private Game game;

  @ManyToOne
  @JoinColumn(name = "owner_id", nullable = false)
  private User owner;

  private String color;
}
