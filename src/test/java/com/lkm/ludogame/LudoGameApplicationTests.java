package com.lkm.ludogame;

import com.lkm.ludogame.model.CellType;
import com.lkm.ludogame.model.GameState;
import com.lkm.ludogame.model.LudoPlayer;
import com.lkm.ludogame.piece.Piece;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LudoGameApplicationTests {

  @Autowired
  LudoGame ludoGame;
  private static final Random dice = new Random();

  @Test
  public void testLudoGame() {
    List<Long> playerIds = List.of(1l, 2l, 3l, 4l);
    GameState gameState = ludoGame.createGame(playerIds);
    int moveCount = 0;
    while (moveCount < 1000 && gameState.getCurrentPlayer() != -1) {
      LudoPlayer currentPlayer = gameState.getPlayerByPlayerId(gameState.getCurrentPlayer());
      int diceValue = dice.nextInt(6) + 1;
      Piece selectedPiece = selectPiece(currentPlayer.getPieceList(), diceValue);
      
      // Debug logging
      if (moveCount % 1000 == 0) {
        System.out.println("Move " + moveCount + ": Player " + currentPlayer.getPlayerId() + 
            " rolled " + diceValue + ", selected piece " + selectedPiece.getId() +
            " at cell " + (selectedPiece.getCurrentCell() != null ? 
                selectedPiece.getCurrentCell().getId() : "null"));
      }
      
      gameState = ludoGame.move(gameState.getId(), diceValue, currentPlayer.getPlayerId(),
          selectedPiece.getId());
      moveCount++;
    }
    
    if (gameState.getCurrentPlayer() == -1) {
      System.out.println("Game completed in " + moveCount + " moves!");
    } else {
      System.out.println("Game did not complete after " + moveCount + " moves");
      // Print final state of all pieces
      for (LudoPlayer player : gameState.getPlayers()) {
        System.out.println("Player " + player.getPlayerId() + " pieces:");
        for (Piece piece : player.getPieceList()) {
          System.out.println("  Piece " + piece.getId() + ": " + 
              (piece.getCurrentCell() != null ? 
                  "cell " + piece.getCurrentCell().getId() + 
                  " type " + piece.getCurrentCell().getCellType() : 
                  "not on board"));
        }
      }
    }
  }

  public Piece selectPiece(List<Piece> pieceList, int diceValue) {
    // First try to find a piece that can enter home path
    for (Piece piece : pieceList) {
      if (piece.getCurrentCell() != null && 
          piece.getCurrentCell().getCellType() == CellType.HOME_PATH) {
        return piece;
      }
    }
    
    // If dice is 6, try to find a piece that's not on board
    if (diceValue == 6) {
      for (Piece piece : pieceList) {
        if (piece.getCurrentCell() == null) {
          return piece;
        }
      }
    }
    
    // Otherwise, find any piece that's on the board and not in home
    for (Piece piece : pieceList) {
      if (piece.getCurrentCell() != null && 
          piece.getCurrentCell().getCellType() != CellType.HOME) {
        return piece;
      }
    }
    
    // If no valid piece found, return the last piece
    return pieceList.getLast();
  }

}
