package org.example.marrakech.controller;

import org.example.marrakech.dto.CarpetPlacementRequest;
import org.example.marrakech.entity.Carpet;
import org.example.marrakech.entity.CarpetPosition;
import org.example.marrakech.repository.CarpetRepository;
import org.example.marrakech.service.CarpetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carpets")
public class CarpetController {

  private final CarpetRepository carpetRepository;
  private final CarpetService carpetService;

  public CarpetController(CarpetRepository carpetRepository, CarpetService carpetService) {
    this.carpetRepository = carpetRepository;
    this.carpetService = carpetService;
  }

  /**
   * Размещает ковер по двум кликам.
   * Пример запроса:
   * {
   *   "carpetId": 5,
   *   "firstX": 3,
   *   "firstY": 2,
   *   "secondX": 3,
   *   "secondY": 1
   * }
   */
  @PostMapping("/place")
  public ResponseEntity<?> placeCarpet(@RequestBody CarpetPlacementRequest request) {
    Carpet carpet = carpetRepository.findById(request.getCarpetId())
        .orElseThrow(() -> new IllegalArgumentException("Ковер не найден"));
    try {
      carpetService.placeCarpet(carpet, request.getFirstX(), request.getFirstY(), request.getSecondX(), request.getSecondY());
      return ResponseEntity.ok("Ковер размещён успешно.");
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }
}
