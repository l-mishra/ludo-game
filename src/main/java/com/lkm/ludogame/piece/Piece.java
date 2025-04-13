package com.lkm.ludogame.piece;

import com.lkm.ludogame.model.Cell;
import com.lkm.ludogame.model.CellType;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

@Getter
@Setter
@EqualsAndHashCode
public class Piece {

  private final int id;
  private final Color color;
  private Cell currentCell;
  private boolean isFinished;

  public Piece(int id, Color color, Cell currentCell, boolean isFinished) {
    this.id = id;
    this.color = color;
    this.currentCell = currentCell;
    this.isFinished = isFinished;
  }

  @Override
  public String toString() {
    return "Piece{" +
        "id=" + id +
        ", color=" + color +
        ", currentCellId=" + (currentCell != null ? currentCell.getId() : "null") +
        ", isFinished=" + isFinished +
        '}';
  }
}
