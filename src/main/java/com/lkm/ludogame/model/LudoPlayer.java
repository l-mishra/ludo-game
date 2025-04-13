package com.lkm.ludogame.model;

import com.lkm.ludogame.piece.Piece;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LudoPlayer {

  private long playerId;
  private List<Piece> pieceList;

}
