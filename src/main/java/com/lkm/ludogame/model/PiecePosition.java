package com.lkm.ludogame.model;

import com.lkm.ludogame.piece.Color;

/**
 * Where a piece is on the board — owned by {@link LudoBoard} only; {@link com.lkm.ludogame.piece.Piece}
 * does not carry a cell reference.
 */
public sealed interface PiecePosition permits PiecePosition.OffBoard, PiecePosition.MainTrack,
    PiecePosition.HomeLane {

  PiecePosition OFF_BOARD = new OffBoard();

  /** Yard — not on the track yet. */
  record OffBoard() implements PiecePosition {}

  /** On the shared outer ring, index 0–51 (inclusive). */
  record MainTrack(int index) implements PiecePosition {
    public MainTrack {
      if (index < 0 || index > 51) {
        throw new IllegalArgumentException("main index out of range: " + index);
      }
    }
  }

  /**
   * On this color's home column: step 0 = first tile after turning in, 5 = final home (finished).
   */
  record HomeLane(Color color, int step) implements PiecePosition {
    public HomeLane {
      if (step < 0 || step > 5) {
        throw new IllegalArgumentException("home step out of range: " + step);
      }
    }

    public boolean isFinishedSquare() {
      return step == 5;
    }
  }
}
