package org.example.marrakech.service;

import org.example.marrakech.entity.Carpet;
import org.example.marrakech.entity.CarpetPosition;
import org.example.marrakech.entity.CarpetPositionId;
import org.example.marrakech.repository.CarpetPositionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CarpetService {

  private final CarpetPositionRepository carpetPositionRepository;

  public CarpetService(CarpetPositionRepository carpetPositionRepository) {
    this.carpetPositionRepository = carpetPositionRepository;
  }

  /**
   * Размещает ковер (размер 2x1) по выбору игрока.
   * Игрок кликает по двум клеткам:
   * - Первая клетка должна быть соседней по стороне с текущей позицией Ассама.
   * - Вторая клетка должна быть соседней по стороне с первой клеткой.
   *
   * Если обе клетки уже заняты, выбрасывается исключение.
   *
   * @param carpet  Ковер, для которого задаётся позиция (уже создан и имеет свой carpetId)
   * @param firstX  Координата X первой выбранной клетки
   * @param firstY  Координата Y первой выбранной клетки
   * @param secondX Координата X второй выбранной клетки
   * @param secondY Координата Y второй выбранной клетки
   * @throws IllegalArgumentException если клетки не соседние, выходят за пределы поля или обе заняты
   */
  @Transactional
  public void placeCarpet(Carpet carpet, int firstX, int firstY, int secondX, int secondY) {
    // Проверка: клетки не должны совпадать
    if (firstX == secondX && firstY == secondY) {
      throw new IllegalArgumentException("Выбраны одинаковые клетки.");
    }
    // Проверка: клетки должны быть соседними по стороне
    if (!isAdjacent(firstX, firstY, secondX, secondY)) {
      throw new IllegalArgumentException("Вторая клетка должна быть соседней с первой.");
    }
    // Проверка: клетки в пределах поля 7x7
    if (!isValidCoordinate(firstX, firstY) || !isValidCoordinate(secondX, secondY)) {
      throw new IllegalArgumentException("Одна из клеток выходит за пределы поля.");
    }

    Long gameId = carpet.getGame().getId();

    boolean firstOccupied = carpetPositionRepository.findByGameAndPosition(gameId, firstX, firstY).isPresent();
    boolean secondOccupied = carpetPositionRepository.findByGameAndPosition(gameId, secondX, secondY).isPresent();

    // Если обе клетки заняты, невозможно разместить ковёр
    if (firstOccupied && secondOccupied) {
      throw new IllegalArgumentException("Обе выбранные клетки уже заняты; ковёр можно перекрыть только наполовину.");
    }

    // Создаем записи для обоих положений ковра.
    // Даже если одна клетка занята, мы создаем записи для нового ковра,
    // что означает частичное перекрытие.
    CarpetPositionId id1 = new CarpetPositionId(carpet.getCarpetId(), firstX, firstY);
    CarpetPosition pos1 = new CarpetPosition();
    pos1.setId(id1);
    pos1.setCarpet(carpet);

    CarpetPositionId id2 = new CarpetPositionId(carpet.getCarpetId(), secondX, secondY);
    CarpetPosition pos2 = new CarpetPosition();
    pos2.setId(id2);
    pos2.setCarpet(carpet);

    carpetPositionRepository.save(pos1);
    carpetPositionRepository.save(pos2);
  }

  private boolean isAdjacent(int x1, int y1, int x2, int y2) {
    // Соседние по стороне: разница по одной координате равна 1, другая равна 0
    return (Math.abs(x1 - x2) == 1 && y1 == y2) ||
        (Math.abs(y1 - y2) == 1 && x1 == x2);
  }

  private boolean isValidCoordinate(int x, int y) {
    return x >= 0 && x < 7 && y >= 0 && y < 7;
  }
}
