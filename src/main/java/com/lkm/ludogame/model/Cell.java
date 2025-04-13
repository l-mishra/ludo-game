package com.lkm.ludogame.model;

import com.lkm.ludogame.piece.Piece;
import com.lkm.ludogame.piece.Color;
import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class Cell {

  private int id;
  private CellType cellType;
  private Color color; // null for neutral cells
  private List<Piece> pieceList = new ArrayList<>();
  private Cell nextCell;
  private Cell homeEntry;

  public Cell(int id, Color color) {
    this.id = id;
    this.color = color;
  }

  public boolean isSafe() {
    return cellType == CellType.SAFE || cellType == CellType.START || cellType == CellType.HOME_PATH
        || cellType == CellType.HOME;
  }

  @Override
  public String toString() {
    return "Cell{" +
        "id=" + id +
        ", cellType=" + cellType +
        ", color=" + color +
        ", pieceCount=" + pieceList.size() +
        ", nextCellId=" + (nextCell != null ? nextCell.id : "null") +
        ", homeEntryId=" + (homeEntry != null ? homeEntry.id : "null") +
        '}';
  }
}
