package com.lkm.ludogame.piece;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Game piece identity only. Position is tracked by {@link com.lkm.ludogame.model.LudoBoard}.
 */
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Piece {

  @EqualsAndHashCode.Include
  private final int id;
  private final Color color;
  private boolean finished;

  public Piece(int id, Color color) {
    this.id = id;
    this.color = color;
    this.finished = false;
  }

  @Override
  public String toString() {
    return "Piece{" + "id=" + id + ", color=" + color + ", finished=" + finished + '}';
  }
}
