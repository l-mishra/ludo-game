package com.lkm.ludogame;

import com.lkm.ludogame.board.path.CellsMetadata;
import com.lkm.ludogame.model.Cell;
import com.lkm.ludogame.model.GameState;
import com.lkm.ludogame.model.LudoBoard;
import com.lkm.ludogame.model.LudoPlayer;
import com.lkm.ludogame.piece.Color;
import com.lkm.ludogame.piece.Piece;
import com.lkm.ludogame.repository.InMemoryGameRepository;
import com.lkm.ludogame.service.MoveService;
import com.lkm.ludogame.validator.MoveValidator;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LudoGame {

  private final MoveService moveService;
  private final InMemoryGameRepository gameRepository;
  private final CellsMetadata cellsMetadata;
  private final MoveValidator moveValidator;

  @Autowired
  public LudoGame(MoveService moveService, InMemoryGameRepository gameRepository,
      CellsMetadata cellsMetadata, MoveValidator moveValidator) {
    this.moveService = moveService;
    this.gameRepository = gameRepository;
    this.cellsMetadata = cellsMetadata;
    this.moveValidator = moveValidator;
  }

  public GameState createGame(List<Long> playerIds) {
    int pieceId = 0;
    LudoBoard ludoBoard = initialiseBoard();
    List<LudoPlayer> ludoPlayers = new ArrayList<>();
    for (Color color : Color.values()) {
      Long playerId = playerIds.get(color.ordinal());
      LudoPlayer player = LudoPlayer.builder().playerId(playerId).pieceList(new ArrayList<>())
          .build();
      // Create 4 pieces for each player
      for (int i = 0; i < 4; i++) {
        player.getPieceList().add(new Piece(pieceId++, color, null, false));
      }
      ludoPlayers.add(player);
    }
    GameState gameState = GameState.builder().id(UUID.randomUUID().toString()).board(ludoBoard)
        .players(ludoPlayers).currentPlayer(ludoPlayers.getFirst().getPlayerId())
        .build();
    gameRepository.save(gameState);
    return gameState;
  }

  private LudoBoard initialiseBoard() {
    LudoBoard ludoBoard = new LudoBoard();
    cellsMetadata.initialiseMainPath(ludoBoard.getMainPath());
    for (Color color : Color.values()) {
      cellsMetadata.initialiseHomePath(color, ludoBoard.getColorToHomePathConsumer());
    }
    cellsMetadata.connectHomePaths(ludoBoard);
    return ludoBoard;
  }

  public GameState move(String gameId, int diceValue, long playerId, int pieceId) {
    GameState gameState = gameRepository.findActiveGameStateById(gameId);
    // Validate if it's the current player's turn
    if (gameState.getCurrentPlayer() != playerId) {
      return gameState; // Return current state if it's not the player's turn
    }
    
    Piece piece = gameState.getPieceByPlayerId(playerId, pieceId);
    if (!moveValidator.isValidMove(gameState.getBoard(), piece, diceValue)) {
      setNextPlayerId(gameState, playerId);
      gameRepository.save(gameState);
      return gameState;
    }
    
    Cell targetCell = moveService.calculateNextPosition(gameState.getBoard(), piece, diceValue);
    moveService.move(piece, targetCell);
    boolean isPieceCaptured = moveService.capturePiece(piece, targetCell);
    
    // Check if current player has won
    LudoPlayer currentPlayer = gameState.getPlayerByPlayerId(playerId);
    if (moveService.isPlayerWinner(currentPlayer)) {
      System.out.println("we have winner here: " + currentPlayer);
      gameState.setCurrentPlayer(-1); // -1 indicates game is over
      gameRepository.archiveGame(gameId);
      return gameState;
    }
    
    // Set next player based on dice value and move outcome
    if (diceValue == 6) {
      // Player gets another turn if they rolled a 6
      gameState.setCurrentPlayer(playerId);
    } else {
      setNextPlayerId(gameState, playerId);
    }
    
    gameRepository.save(gameState);
    return gameState;
  }

  private void setNextPlayerId(GameState gameState, long playerId) {
    int indexOfCurrentPlayer = IntStream.range(0, 4)
        .filter(i -> gameState.getPlayers().get(i).getPlayerId() == playerId).findFirst().stream()
        .findFirst().getAsInt();
    int nextPlayerIndex = (indexOfCurrentPlayer + 1) % 4;
    gameState.setCurrentPlayer(gameState.getPlayers().get(nextPlayerIndex).getPlayerId());
  }

}
