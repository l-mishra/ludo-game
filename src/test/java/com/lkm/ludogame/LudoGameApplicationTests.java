package com.lkm.ludogame;

import com.lkm.ludogame.model.GameState;
import com.lkm.ludogame.model.LudoBoard;
import com.lkm.ludogame.model.LudoPlayer;
import com.lkm.ludogame.model.PiecePosition;
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
    List<Long> playerIds = List.of(1L, 2L, 3L, 4L);
    GameState gameState = ludoGame.createGame(playerIds);
    int moveCount = 0;
    while (moveCount < 1000 && gameState.getCurrentPlayer() != -1) {
      LudoPlayer currentPlayer = gameState.getPlayerByPlayerId(gameState.getCurrentPlayer());
      int diceValue = dice.nextInt(6) + 1;
      Piece selectedPiece = selectPiece(gameState.getBoard(), currentPlayer.getPieceList(), diceValue);

      if (moveCount % 1000 == 0) {
        PiecePosition pos = gameState.getBoard().positionOf(selectedPiece);
        System.out.println("Move " + moveCount + ": Player " + currentPlayer.getPlayerId() +
            " rolled " + diceValue + ", selected piece " + selectedPiece.getId() +
            " at " + pos);
      }

      gameState = ludoGame.move(gameState.getId(), diceValue, currentPlayer.getPlayerId(),
          selectedPiece.getId());
      moveCount++;
    }

    if (gameState.getCurrentPlayer() == -1) {
      System.out.println("Game completed in " + moveCount + " moves!");
    } else {
      System.out.println("Game did not complete after " + moveCount + " moves");
      for (LudoPlayer player : gameState.getPlayers()) {
        System.out.println("Player " + player.getPlayerId() + " pieces:");
        for (Piece piece : player.getPieceList()) {
          System.out.println("  Piece " + piece.getId() + ": " + gameState.getBoard().positionOf(piece));
        }
      }
    }
  }

  private Piece selectPiece(LudoBoard board, List<Piece> pieceList, int diceValue) {
    for (Piece piece : pieceList) {
      PiecePosition pos = board.positionOf(piece);
      if (pos instanceof PiecePosition.HomeLane hl && hl.step() < 5) {
        return piece;
      }
    }
    if (diceValue == 6) {
      for (Piece piece : pieceList) {
        if (board.positionOf(piece) instanceof PiecePosition.OffBoard) {
          return piece;
        }
      }
    }
    for (Piece piece : pieceList) {
      PiecePosition pos = board.positionOf(piece);
      if (pos instanceof PiecePosition.MainTrack) {
        return piece;
      }
      if (pos instanceof PiecePosition.HomeLane hl && !hl.isFinishedSquare()) {
        return piece;
      }
    }
    return pieceList.getLast();
  }
}
