package com.lkm.ludogame;

import com.lkm.ludogame.model.GameState;
import com.lkm.ludogame.model.LudoBoard;
import com.lkm.ludogame.model.LudoPlayer;
import com.lkm.ludogame.model.MoveResult;
import com.lkm.ludogame.piece.Color;
import com.lkm.ludogame.piece.Piece;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Orchestrates the game. In-memory session storage lives here; no separate repository layer
 * until persistence or multi-game APIs are in scope.
 */
@Component
public class LudoGame {

  private final Map<String, GameState> activeGames = new ConcurrentHashMap<>();
  private final Map<String, GameState> completedGames = new ConcurrentHashMap<>();

  public GameState createGame(List<Long> playerIds) {
    LudoBoard board = LudoBoard.create();
    List<LudoPlayer> players = new ArrayList<>();
    int pieceId = 0;
    for (Color color : Color.values()) {
      LudoPlayer player = LudoPlayer.builder()
          .playerId(playerIds.get(color.ordinal()))
          .pieceList(new ArrayList<>())
          .build();
      for (int i = 0; i < 4; i++) {
        player.getPieceList().add(new Piece(pieceId++, color));
      }
      players.add(player);
    }
    GameState gameState = GameState.builder()
        .id(UUID.randomUUID().toString())
        .board(board)
        .players(players)
        .currentPlayer(players.getFirst().getPlayerId())
        .build();
    board.registerPieces(gameState);
    activeGames.put(gameState.getId(), gameState);
    return gameState;
  }

  public GameState move(String gameId, int diceValue, long playerId, int pieceId) {
    GameState gameState = activeGames.get(gameId);
    if (gameState == null) {
      throw new IllegalArgumentException("Unknown or finished game: " + gameId);
    }
    if (gameState.getCurrentPlayer() != playerId) {
      return gameState;
    }
    MoveResult result = gameState.getBoard().executeMove(gameState, playerId, pieceId, diceValue);
    if (!result.success()) {
      setNextPlayer(gameState, playerId);
      return gameState;
    }
    if (result.winner().isPresent()) {
      gameState.setCurrentPlayer(-1);
      GameState finished = activeGames.remove(gameId);
      if (finished != null) {
        completedGames.put(gameId, finished);
      }
      return gameState;
    }
    gameState.setCurrentPlayer(diceValue == 6 ? playerId : getNextPlayerId(gameState, playerId));
    return gameState;
  }

  private void setNextPlayer(GameState gameState, long playerId) {
    gameState.setCurrentPlayer(getNextPlayerId(gameState, playerId));
  }

  private long getNextPlayerId(GameState gameState, long playerId) {
    int size = gameState.getPlayers().size();
    int currentIndex = 0;
    for (int i = 0; i < size; i++) {
      if (gameState.getPlayers().get(i).getPlayerId() == playerId) {
        currentIndex = i;
        break;
      }
    }
    return gameState.getPlayers().get((currentIndex + 1) % size).getPlayerId();
  }
}
