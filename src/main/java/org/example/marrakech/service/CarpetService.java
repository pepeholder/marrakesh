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
   * Размещает ковер (размер 2x1) по выбору игрока с учётом правил наложения.
   * placementTurn теперь берется автоматически из объекта Game.
   *
   * @param carpet  Ковер, для которого задается позиция (уже создан, содержит carpetId)
   * @param firstX  Координата X первой выбранной клетки.
   * @param firstY  Координата Y первой выбранной клетки.
   * @param secondX Координата X второй выбранной клетки.
   * @param secondY Координата Y второй выбранной клетки.
   * @throws IllegalArgumentException если клетки не соседние, находятся вне поля или нарушены правила наложения.
   */
  @Transactional
  public void placeCarpet(Carpet carpet, int firstX, int firstY, int secondX, int secondY) {
    // Проверяем, что выбранные клетки различны
    if (firstX == secondX && firstY == secondY) {
      throw new IllegalArgumentException("Выбраны одинаковые клетки.");
    }
    // Проверяем соседство выбранных клеток
    if (!isAdjacent(firstX, firstY, secondX, secondY)) {
      throw new IllegalArgumentException("Клетки должны быть соседними по стороне.");
    }
    // Проверяем допустимость координат (поле 7x7)
    if (!isValidCoordinate(firstX, firstY) || !isValidCoordinate(secondX, secondY)) {
      throw new IllegalArgumentException("Клетки вне поля 7x7.");
    }

    Long gameId = carpet.getGame().getId();
    // Получаем номер хода из объекта Game (currentMoveNumber)
    int placementTurn = carpet.getGame().getCurrentMoveNumber();
    if (placementTurn <= 0) {
      throw new IllegalArgumentException("Номер хода (currentMoveNumber) должен быть положительным.");
    }

    // Проверяем правила наложения для каждой выбранной клетки
    checkOverlayRules(carpet, gameId, firstX, firstY);
    checkOverlayRules(carpet, gameId, secondX, secondY);

    // Дополнительная проверка: если обе клетки уже заняты верхними коврами,
    // и они принадлежат одному и тому же ковру, размещённому в одном ходу,
    // то новый ковер полностью перекроет чужой – запрещается.
    Optional<CarpetPosition> topPos1 = carpetPositionRepository.findTopByGameAndPositionOrderByPlacementTurnDesc(gameId, firstX, firstY);
    Optional<CarpetPosition> topPos2 = carpetPositionRepository.findTopByGameAndPositionOrderByPlacementTurnDesc(gameId, secondX, secondY);
    if (topPos1.isPresent() && topPos2.isPresent()) {
      Carpet topCarpet1 = topPos1.get().getCarpet();
      Carpet topCarpet2 = topPos2.get().getCarpet();
      if (topCarpet1.getCarpetId().equals(topCarpet2.getCarpetId())) {
        int turn1 = topPos1.get().getPlacementTurn();
        int turn2 = topPos2.get().getPlacementTurn();
        if (turn1 == turn2) {
          throw new IllegalArgumentException("Нельзя полностью перекрыть один и тот же ковёр.");
        }
      }
    }

    CarpetPosition pos1 = createCarpetPosition(carpet, firstX, firstY, placementTurn);
    CarpetPosition pos2 = createCarpetPosition(carpet, secondX, secondY, placementTurn);

    carpetPositionRepository.save(pos1);
    carpetPositionRepository.save(pos2);
  }

  /**
   * Размещает ковер после перемещения Ассама.
   * Проверяет, что ни одна из выбранных клеток не совпадает с позицией Ассама,
   * и что первая выбранная клетка является соседней с конечной позицией Ассама.
   *
   * @param carpet  Ковер, принадлежащий текущему игроку.
   * @param finalX  Конечная позиция Ассама по X (из объекта Game).
   * @param finalY  Конечная позиция Ассама по Y (из объекта Game).
   * @param firstX  Координата X первой выбранной клетки.
   * @param firstY  Координата Y первой выбранной клетки.
   * @param secondX Координата X второй выбранной клетки.
   * @param secondY Координата Y второй выбранной клетки.
   */
  public void placeCarpetAfterMove(Carpet carpet, int finalX, int finalY,
                                   int firstX, int firstY, int secondX, int secondY) {
    // Запрещаем размещать ковер на клетке, где находится Ассам
    if ((firstX == finalX && firstY == finalY) || (secondX == finalX && secondY == finalY)) {
      throw new IllegalArgumentException("Нельзя размещать ковёр на клетке, где находится Ассам.");
    }
    // Первая клетка должна быть соседней с конечной позицией Ассама
    if (!isAdjacent(finalX, finalY, firstX, firstY)) {
      throw new IllegalArgumentException("Первая клетка должна быть соседней с конечной позицией Ассама.");
    }
    placeCarpet(carpet, firstX, firstY, secondX, secondY);
  }

  private CarpetPosition createCarpetPosition(Carpet carpet, int x, int y, int placementTurn) {
    CarpetPositionId id = new CarpetPositionId(carpet.getCarpetId(), x, y);
    CarpetPosition position = new CarpetPosition();
    position.setId(id);
    position.setCarpet(carpet);
    position.setPlacementTurn(placementTurn);
    return position;
  }

  /**
   * Проверяет, можно ли разместить ковер на данной клетке с учётом уже размещённых.
   * Если на клетке уже лежит верхний ковер, и он принадлежит тому же владельцу, размещение запрещается.
   *
   * @param carpet Ковер, который кладется.
   * @param gameId Идентификатор игры.
   * @param x      Координата клетки.
   * @param y      Координата клетки.
   */
  private void checkOverlayRules(Carpet carpet, Long gameId, int x, int y) {
    Optional<CarpetPosition> existingOpt = carpetPositionRepository.findTopByGameAndPositionOrderByPlacementTurnDesc(gameId, x, y);
    if (existingOpt.isPresent()) {
      Carpet topCarpet = existingOpt.get().getCarpet();
      if (topCarpet.getOwner().getId().equals(carpet.getOwner().getId())) {
        throw new IllegalArgumentException("Нельзя размещать ковёр на клетке, где сверху лежит ваш собственный ковёр.");
      }
    }
  }

  private boolean isAdjacent(int x1, int y1, int x2, int y2) {
    return (Math.abs(x1 - x2) == 1 && y1 == y2) ||
        (Math.abs(y1 - y2) == 1 && x1 == x2);
  }

  private boolean isValidCoordinate(int x, int y) {
    return x >= 0 && x < 7 && y >= 0 && y < 7;
  }
}
