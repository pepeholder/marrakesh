package org.example.marrakech.service;

import org.example.marrakech.entity.Carpet;
import org.example.marrakech.entity.CarpetPosition;
import org.example.marrakech.entity.CarpetPositionId;
import org.example.marrakech.repository.CarpetPositionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CarpetService {

  private final CarpetPositionRepository carpetPositionRepository;

  public CarpetService(CarpetPositionRepository carpetPositionRepository) {
    this.carpetPositionRepository = carpetPositionRepository;
  }

  /**
   * Размещает ковер (размер 2x1) по выбору игрока.
   * Игрок выбирает две клетки, которые должны быть соседними по стороне.
   * Если обе клетки уже заняты, выбрасывается исключение.
   *
   * @param carpet  Ковер, для которого задаётся позиция (уже создан, содержит carpetId)
   * @param firstX  Координата X первой выбранной клетки
   * @param firstY  Координата Y первой выбранной клетки
   * @param secondX Координата X второй выбранной клетки
   * @param secondY Координата Y второй выбранной клетки
   * @throws IllegalArgumentException если клетки не соседние, выходят за пределы поля или нарушены правила наложения
   */
  @Transactional
  public void placeCarpet(Carpet carpet, int firstX, int firstY, int secondX, int secondY) {
    // Проверка: клетки не должны совпадать
    if (firstX == secondX && firstY == secondY) {
      throw new IllegalArgumentException("Выбраны одинаковые клетки.");
    }
    // Проверка: клетки должны быть соседними по стороне
    if (!isAdjacent(firstX, firstY, secondX, secondY)) {
      throw new IllegalArgumentException("Клетки должны быть соседними по стороне.");
    }
    // Проверка: клетки в пределах поля 7x7
    if (!isValidCoordinate(firstX, firstY) || !isValidCoordinate(secondX, secondY)) {
      throw new IllegalArgumentException("Клетки вне поля 7x7.");
    }

    Long gameId = carpet.getGame().getId();

    // Выполняем проверки наложения для каждой выбранной клетки
    checkOverlayRules(carpet, gameId, firstX, firstY);
    checkOverlayRules(carpet, gameId, secondX, secondY);

    CarpetPosition pos1 = createCarpetPosition(carpet, firstX, firstY);
    CarpetPosition pos2 = createCarpetPosition(carpet, secondX, secondY);

    carpetPositionRepository.save(pos1);
    carpetPositionRepository.save(pos2);
  }

  /**
   * Проверяет, можно ли разместить ковер на данной клетке с учетом наложения.
   * Если на клетке уже лежит верхний ковёр, то:\n" +
   * - Если его цвет совпадает с цветом размещаемого ковра, выбрасывается исключение;\n" +
   * - Если эта клетка уже полностью занята данным ковром, выбрасывается исключение.
   *
   * @param carpet Ковер, который кладется
   * @param gameId Идентификатор игры
   * @param x      Координата клетки
   * @param y      Координата клетки
   */
  private void checkOverlayRules(Carpet carpet, Long gameId, int x, int y) {
    Optional<CarpetPosition> existingOpt = carpetPositionRepository.findTopByGameAndPosition(gameId, x, y);
    if (existingOpt.isPresent()) {
      Carpet topCarpet = existingOpt.get().getCarpet();
      // Нельзя положить ковёр на другой ковёр такого же цвета
      if (topCarpet.getColor().equals(carpet.getColor())) {
        throw new IllegalArgumentException("Нельзя накрыть ковёр того же цвета.");
      }
      // Нельзя полностью перекрыть один и тот же ковёр (тот же carpetId)
      if (topCarpet.getCarpetId().equals(carpet.getCarpetId())) {
        throw new IllegalArgumentException("Нельзя полностью перекрыть один и тот же ковёр.");
      }
    }
  }

  private CarpetPosition createCarpetPosition(Carpet carpet, int x, int y) {
    CarpetPositionId id = new CarpetPositionId(carpet.getCarpetId(), x, y);
    CarpetPosition position = new CarpetPosition();
    position.setId(id);
    position.setCarpet(carpet);
    return position;
  }

  private boolean isAdjacent(int x1, int y1, int x2, int y2) {
    // Соседние по стороне: разница по одной координате равна 1, другая равна 0
    return (Math.abs(x1 - x2) == 1 && y1 == y2) ||
        (Math.abs(y1 - y2) == 1 && x1 == x2);
  }

  private boolean isValidCoordinate(int x, int y) {
    return x >= 0 && x < 7 && y >= 0 && y < 7;
  }

  /**
   * Метод для размещения ковра после перемещения Ассама.
   * Проверяет, что первая выбранная клетка соседняя с конечной позицией Ассама,
   * и что ни одна из выбранных клеток не совпадает с конечной позицией Ассама.
   *
   * @param carpet  Ковер, принадлежащий текущему игроку
   * @param finalX  Конечная позиция Ассама по X (из Game)
   * @param finalY  Конечная позиция Ассама по Y (из Game)
   * @param firstX  Координата X первой выбранной клетки
   * @param firstY  Координата Y первой выбранной клетки
   * @param secondX Координата X второй выбранной клетки
   * @param secondY Координата Y второй выбранной клетки
   */
  public void placeCarpetAfterMove(Carpet carpet, int finalX, int finalY, int firstX, int firstY, int secondX, int secondY) {
    // Проверяем, что ни одна из выбранных клеток не совпадает с позицией Ассама
    if ((firstX == finalX && firstY == finalY) || (secondX == finalX && secondY == finalY)) {
      throw new IllegalArgumentException("Нельзя размещать ковёр на клетке, где находится Ассам.");
    }
    // Проверяем, что первая клетка соседняя с конечной позицией Ассама
    if (!isAdjacent(finalX, finalY, firstX, firstY)) {
      throw new IllegalArgumentException("Первая клетка должна быть соседней с конечной позицией Ассама.");
    }
    // Затем вызываем основной метод размещения ковра для проверки и сохранения позиций
    placeCarpet(carpet, firstX, firstY, secondX, secondY);
  }
}
