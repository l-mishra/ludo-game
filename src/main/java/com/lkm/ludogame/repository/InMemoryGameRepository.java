package com.lkm.ludogame.repository;

import static java.util.stream.Collectors.toList;

import com.lkm.ludogame.model.GameState;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Repository;

@Repository
@Data
@Getter
@Setter
public class InMemoryGameRepository {

  private final Map<String, GameState> activeGames = new ConcurrentHashMap<>();
  private final Map<String, GameState> gameHistory = new ConcurrentHashMap<>();

  public void save(GameState gameState) {
    if (gameState == null) {
      throw new IllegalArgumentException("can't save the game state");
    }
    activeGames.put(gameState.getId(), gameState);
  }

  public GameState findActiveGameStateById(String gameId) {
    GameState gameState = activeGames.get(gameId);
    return gameState;
  }

  public void archiveGame(String gameId) {
    GameState gameState = activeGames.get(gameId);
    if (gameState == null) {
      return;
    }
    gameHistory.put(gameId, gameState);
  }

  // Additional Utility methods
  public List<GameState> findActiveGamesByPlayerId(Long playerId) {
    return activeGames.values().stream().filter(gameState -> gameState.getPlayers().stream().
        anyMatch(player -> player.getPlayerId() == playerId)).collect(toList());
  }
}
