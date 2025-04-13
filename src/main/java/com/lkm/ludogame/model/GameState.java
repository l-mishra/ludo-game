package com.lkm.ludogame.model;

import com.lkm.ludogame.piece.Piece;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Builder
@Data
@Getter
@Setter
public class GameState {

  private String id;
  private List<LudoPlayer> players;
  private LudoBoard board;
  private long currentPlayer;

  public Piece getPieceByPlayerId(long playerId, int pieceId) {
    return this.players.stream().filter(player -> player.getPlayerId() == playerId)
        .flatMap(player -> player.getPieceList().stream()).filter(piece -> piece.getId() == pieceId)
        .findAny().orElse(null);
  }

  public LudoPlayer getPlayerByPlayerId(long playerId) {
    return players.stream().filter(player -> player.getPlayerId() == playerId).findFirst().get();
  }
}
