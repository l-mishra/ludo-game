package com.lkm.ludogame.model;

import com.lkm.ludogame.piece.Color;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import lombok.Getter;

@Getter
public class LudoBoard {

  private final List<Cell> mainPath = new ArrayList<>();
  private final Map<Color, List<Cell>> homePaths = new EnumMap<>(Color.class);

  public Cell getHomeEntryPoint(Color color) {
    return homePaths.get(color).get(0);
  }

  public BiConsumer<Color, List<Cell>> getColorToHomePathConsumer() {
    return homePaths::put;
  }

  public Cell getMainPathCellByIndex(int index) {
    if (index < 0 || index > 51) {
      throw new IllegalArgumentException("cell index out of bound");
    }
    return mainPath.get(index);
  }

  public List<Cell> getHomePath(Color color) {
    return homePaths.get(color);
  }
}
