package com.lkm.ludogame.service;

import com.lkm.ludogame.board.path.CellsMetadata;
import com.lkm.ludogame.model.Cell;
import com.lkm.ludogame.model.CellType;
import com.lkm.ludogame.model.LudoBoard;
import com.lkm.ludogame.model.LudoPlayer;
import com.lkm.ludogame.piece.Piece;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class MoveService {
  private final CellsMetadata cellsMetadata;

  @Autowired
  public MoveService(CellsMetadata cellsMetadata) {
    this.cellsMetadata = cellsMetadata;
  }

  public Cell calculateNextPosition(LudoBoard ludoBoard, Piece piece, int diceValue) {
    Cell curCell = piece.getCurrentCell();
    if(curCell == null){
      if(diceValue == 6){
        return ludoBoard.getMainPathCellByIndex(cellsMetadata.getEntryPoint(piece.getColor()));
      }
      return null;
    }

    // If piece is already in home path
    if (curCell.getCellType() == CellType.HOME_PATH) {
      Cell targetCell = curCell;
      for (int i = 0; i < diceValue; i++) {
        if (targetCell.getNextCell() == null) {
          return targetCell; // Can't move further in home path
        }
        targetCell = targetCell.getNextCell();
      }
      return targetCell;
    }

    // First calculate the final position on main path
    Cell finalMainPathCell = curCell;
    int homeEntryIndex = cellsMetadata.getHomeEntryPoint(piece.getColor());
    int currentIndex = ludoBoard.getMainPath().indexOf(curCell);
    
    // Calculate the number of steps needed to reach home entry point
    int stepsToHomeEntry;
    if (currentIndex <= homeEntryIndex) {
      stepsToHomeEntry = homeEntryIndex - currentIndex;
    } else {
      stepsToHomeEntry = 52 - currentIndex + homeEntryIndex;
    }

    // If we can reach home entry point with the current dice value
    if (diceValue >= stepsToHomeEntry) {
      // Move to home entry point
      for (int i = 0; i < stepsToHomeEntry; i++) {
        finalMainPathCell = finalMainPathCell.getNextCell();
      }
      // If we land exactly on home entry point, enter home path
      if (diceValue == stepsToHomeEntry && finalMainPathCell.getHomeEntry() != null) {
        return finalMainPathCell.getHomeEntry();
      }
      // If we overshoot, continue moving remaining steps
      int remainingSteps = diceValue - stepsToHomeEntry;
      for (int i = 0; i < remainingSteps; i++) {
        finalMainPathCell = finalMainPathCell.getNextCell();
      }
    } else {
      // If we can't reach home entry point, just move normally
      for (int i = 0; i < diceValue; i++) {
        finalMainPathCell = finalMainPathCell.getNextCell();
      }
    }

    return finalMainPathCell;
  }

  public boolean capturePiece(Piece movingPiece, Cell targetCell) {
    if (targetCell.isSafe()) {
      return false;
    }
    if (CollectionUtils.isEmpty(targetCell.getPieceList())) {
      return false;
    }
    Piece pieceToCapture = targetCell.getPieceList().getFirst();
    if (pieceToCapture.getColor().equals(movingPiece.getColor())) {
      return false;
    }
    return resetCapturedPiece(pieceToCapture);
  }

  public boolean resetCapturedPiece(Piece piece) {
    Cell currentCell = piece.getCurrentCell();
    currentCell.getPieceList().clear();
    piece.setCurrentCell(null);
    return true;
  }

  public void move(Piece piece, Cell targetCell) {
    Cell currentCell = piece.getCurrentCell();
    if (currentCell != null) {
      currentCell.getPieceList().clear();
    }
    piece.setCurrentCell(targetCell);
    targetCell.getPieceList().add(piece);
    
    // Check if piece has reached home
    if (targetCell.getCellType() == CellType.HOME) {
      piece.setFinished(true);
    }
  }

  public boolean isPlayerWinner(LudoPlayer player) {
    return player.getPieceList().stream().allMatch(Piece::isFinished);
  }

}
