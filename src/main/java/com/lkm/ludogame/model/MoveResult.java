package com.lkm.ludogame.model;

import java.util.Optional;

public record MoveResult(boolean success, boolean captured, Optional<Long> winner) {

  public static MoveResult invalid() {
    return new MoveResult(false, false, Optional.empty());
  }
}
