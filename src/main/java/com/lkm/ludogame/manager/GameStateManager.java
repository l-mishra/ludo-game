package com.lkm.ludogame.manager;

import com.lkm.ludogame.model.GameState;

public interface GameStateManager {
  void save(GameState gameState);
  void getGameStateById(String id);
}
