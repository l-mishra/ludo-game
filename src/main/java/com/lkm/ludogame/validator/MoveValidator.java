package com.lkm.ludogame.validator;

import com.lkm.ludogame.board.path.CellsMetadata;
import com.lkm.ludogame.model.Cell;
import com.lkm.ludogame.model.CellType;
import com.lkm.ludogame.model.LudoBoard;
import com.lkm.ludogame.piece.Piece;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MoveValidator {

  private final CellsMetadata cellsMetadata;

  @Autowired
  public MoveValidator(CellsMetadata cellsMetadata) {
    this.cellsMetadata = cellsMetadata;
  }

  public boolean isValidMove(LudoBoard board, Piece piece, int diceValue) {
    Cell currentCell = piece.getCurrentCell();

    // If piece is not on board yet, it can only move if dice value is 6
    if (currentCell == null) {
      return diceValue == 6;
    }

    //if piece is already in home path
    if (currentCell.getCellType() == CellType.HOME_PATH) {
      return validateHomePathMove(board, piece, diceValue);
    }
    if (isNearHomeEntryPoint(board, piece, diceValue)) {
      return validateHomeEntryMove(board, piece, diceValue);
    }
    return true;
  }

  private boolean isNearHomeEntryPoint(LudoBoard board, Piece piece, int diceValue) {
    Cell homeEntryPoint = board.getMainPathCellByIndex(cellsMetadata.getHomeEntryPoint(piece.getColor()));
    Cell currentCell = piece.getCurrentCell();
    int stepsToEntry = 0;
   
    while (currentCell != homeEntryPoint && stepsToEntry < diceValue) {
      currentCell = currentCell.getNextCell();
      stepsToEntry++;
    }
    return currentCell == homeEntryPoint && stepsToEntry <= diceValue;
  }

  private boolean validateHomeEntryMove(LudoBoard board, Piece piece, int diceValue) {
    Cell currentCell = piece.getCurrentCell();
    Cell homeEntryPoint = board.getHomeEntryPoint(piece.getColor());
    int stepsToEntry = 0;
    Cell checkCell = currentCell;
    while (checkCell != homeEntryPoint) {
      if (checkCell.getHomeEntry() != null) {
        checkCell = checkCell.getHomeEntry();
      } else {
        checkCell = checkCell.getNextCell();
      }
      stepsToEntry++;
    }
    int remainingSteps = diceValue - stepsToEntry;
    // check if the remaining steps would overshoot home
    List<Cell> homePath = board.getHomePath(piece.getColor());
    return remainingSteps <= homePath.size();
  }

  private boolean validateHomePathMove(LudoBoard board, Piece piece, int diceValue) {
    List<Cell> homePath = board.getHomePath(piece.getColor());
    int currentIndex = homePath.indexOf(piece.getCurrentCell());
    if (currentIndex == -1) {
      return false; // Piece is not in home path
    }
    int targetIndex = currentIndex + diceValue;
    return targetIndex < homePath.size(); // Allow move if it won't overshoot home
  }

}
