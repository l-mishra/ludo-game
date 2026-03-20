package com.lkm.ludogame.model;

import com.lkm.ludogame.piece.Color;
import com.lkm.ludogame.piece.Piece;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Aggregate root: owns rules and all {@link PiecePosition} data.
 * <p>
 * The outer track is a <strong>flat ring</strong> of 52 indices {@code 0..51} — movement uses
 * {@code (index + steps) % 52}, not linked cells. Home columns use linear steps {@code 0..5} per
 * color (see {@link PiecePosition.HomeLane}).
 * </p>
 */
public class LudoBoard {

  private static final int RING_SIZE = 52;
  private static final int HOME_LANE_LENGTH = 6;

  private static final Map<Color, Integer> ENTRY_POINTS = Map.of(
      Color.RED, 0, Color.BLUE, 13, Color.YELLOW, 26, Color.GREEN, 39);
  private static final Map<Color, Integer> HOME_ENTRY_POINTS = Map.of(
      Color.RED, 50, Color.BLUE, 11, Color.YELLOW, 24, Color.GREEN, 37);

  /** Board-owned placement; pieces do not store coordinates. */
  private final Map<Piece, PiecePosition> pieceLocations = new IdentityHashMap<>();

  public static LudoBoard create() {
    return new LudoBoard();
  }

  /** Forward steps from {@code fromIndex} to {@code toIndex} along the ring (increasing index, wrap 51→0). */
  static int forwardStepsOnRing(int fromIndex, int toIndex) {
    if (fromIndex <= toIndex) {
      return toIndex - fromIndex;
    }
    return RING_SIZE - fromIndex + toIndex;
  }

  /** Safe cells on the shared track (star squares). */
  private static boolean isSafeMainIndex(int index) {
    return index % 13 == 0;
  }

  /** Register every piece as off-board before play starts. */
  public void registerPieces(GameState gameState) {
    for (LudoPlayer player : gameState.getPlayers()) {
      for (Piece piece : player.getPieceList()) {
        pieceLocations.put(piece, PiecePosition.OFF_BOARD);
      }
    }
  }

  public PiecePosition positionOf(Piece piece) {
    PiecePosition pos = pieceLocations.get(piece);
    if (pos == null) {
      throw new IllegalStateException("Piece not registered with board: " + piece);
    }
    return pos;
  }

  public MoveResult executeMove(GameState gameState, long playerId, int pieceId, int diceValue) {
    Piece piece = gameState.getPieceByPlayerId(playerId, pieceId);
    if (piece == null) {
      return MoveResult.invalid();
    }
    if (!isValidMove(piece, diceValue)) {
      return MoveResult.invalid();
    }
    PiecePosition targetPos = calculateNextPosition(piece, diceValue);
    if (targetPos == null) {
      return MoveResult.invalid();
    }
    boolean captured = captureAt(piece, targetPos, gameState);
    applyMove(piece, targetPos);
    LudoPlayer currentPlayer = gameState.getPlayerByPlayerId(playerId);
    if (isPlayerWinner(currentPlayer)) {
      return new MoveResult(true, captured, Optional.of(playerId));
    }
    return new MoveResult(true, captured, Optional.empty());
  }

  private boolean isSafePosition(PiecePosition pos) {
    return switch (pos) {
      case PiecePosition.OffBoard off -> false;
      case PiecePosition.MainTrack mt -> isSafeMainIndex(mt.index());
      case PiecePosition.HomeLane hl -> true;
    };
  }

  private boolean captureAt(Piece attacker, PiecePosition target, GameState gameState) {
    if (isSafePosition(target)) {
      return false;
    }
    boolean friendlyOccupant = false;
    Piece enemy = null;
    for (LudoPlayer player : gameState.getPlayers()) {
      for (Piece other : player.getPieceList()) {
        if (other == attacker) {
          continue;
        }
        if (!pieceLocations.get(other).equals(target)) {
          continue;
        }
        if (other.getColor() == attacker.getColor()) {
          friendlyOccupant = true;
        } else {
          enemy = other;
        }
      }
    }
    if (friendlyOccupant) {
      return false;
    }
    if (enemy != null) {
      pieceLocations.put(enemy, PiecePosition.OFF_BOARD);
      enemy.setFinished(false);
      return true;
    }
    return false;
  }

  private void applyMove(Piece piece, PiecePosition targetPos) {
    pieceLocations.put(piece, targetPos);
    if (targetPos instanceof PiecePosition.HomeLane hl && hl.isFinishedSquare()) {
      piece.setFinished(true);
    }
  }

  private boolean isValidMove(Piece piece, int diceValue) {
    PiecePosition pos = pieceLocations.get(piece);
    if (pos == null) {
      return false;
    }
    return switch (pos) {
      case PiecePosition.OffBoard off -> diceValue == 6;
      case PiecePosition.HomeLane hl -> !hl.isFinishedSquare() && validateHomePathMove(hl, diceValue);
      case PiecePosition.MainTrack mt -> {
        if (!isNearHomeEntryPoint(mt.index(), piece.getColor(), diceValue)) {
          yield true;
        }
        yield validateHomeEntryMove(mt.index(), piece.getColor(), diceValue);
      }
    };
  }

  /** True if within {@code dice} forward steps on the ring we can reach the home-entry main index. */
  private boolean isNearHomeEntryPoint(int mainIndex, Color color, int diceValue) {
    int homeEntry = HOME_ENTRY_POINTS.get(color);
    return forwardStepsOnRing(mainIndex, homeEntry) <= diceValue;
  }

  /**
   * Home lane has 6 squares (steps {@code 0..5}). Cost to reach step 0 from main track =
   * ring steps to home-entry index + 1 (step onto first home tile). Extra dice after that advances
   * inside the lane; more than 5 extra would pass the center → illegal.
   */
  private boolean validateHomeEntryMove(int mainIndex, Color color, int diceValue) {
    int homeEntry = HOME_ENTRY_POINTS.get(color);
    int stepsToFirstHomeTile = forwardStepsOnRing(mainIndex, homeEntry) + 1;
    if (diceValue < stepsToFirstHomeTile) {
      return true; // still moving only on outer ring
    }
    int intoHome = diceValue - stepsToFirstHomeTile;
    return intoHome <= 5;
  }

  private boolean validateHomePathMove(PiecePosition.HomeLane hl, int diceValue) {
    return hl.step() + diceValue < HOME_LANE_LENGTH;
  }

  /**
   * Next position after moving {@code dice} steps. From the main ring, {@code stepsToFirstHomeTile}
   * = ring distance to home-entry index + 1 (onto home step 0). Dice beyond that moves along the
   * home lane (steps 0–5); past step 5 is invalid (cannot move).
   */
  private PiecePosition calculateNextPosition(Piece piece, int diceValue) {
    PiecePosition pos = pieceLocations.get(piece);
    if (pos == null) {
      return null;
    }
    Color c = piece.getColor();
    return switch (pos) {
      case PiecePosition.OffBoard off ->
          diceValue == 6 ? new PiecePosition.MainTrack(ENTRY_POINTS.get(c)) : null;
      case PiecePosition.HomeLane hl -> {
        int nextStep = hl.step() + diceValue;
        yield nextStep >= HOME_LANE_LENGTH ? null : new PiecePosition.HomeLane(hl.color(), nextStep);
      }
      case PiecePosition.MainTrack mt -> nextFromMainTrack(mt.index(), c, diceValue);
    };
  }

  private PiecePosition nextFromMainTrack(int mainIndex, Color color, int diceValue) {
    int homeEntry = HOME_ENTRY_POINTS.get(color);
    int stepsToFirstHomeTile = forwardStepsOnRing(mainIndex, homeEntry) + 1;
    if (diceValue < stepsToFirstHomeTile) {
      return new PiecePosition.MainTrack((mainIndex + diceValue) % RING_SIZE);
    }
    int intoHome = diceValue - stepsToFirstHomeTile;
    if (intoHome > 5) {
      return null;
    }
    return new PiecePosition.HomeLane(color, intoHome);
  }

  private boolean isPlayerWinner(LudoPlayer player) {
    return player.getPieceList().stream().allMatch(Piece::isFinished);
  }
}
