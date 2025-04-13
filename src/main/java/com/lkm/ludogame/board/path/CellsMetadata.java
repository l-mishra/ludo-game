package com.lkm.ludogame.board.path;

import com.lkm.ludogame.model.Cell;
import com.lkm.ludogame.model.CellType;
import com.lkm.ludogame.model.LudoBoard;
import com.lkm.ludogame.piece.Color;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import org.springframework.stereotype.Service;

@Service
public class CellsMetadata {

  private final Map<Color, List<Cell>> startCells = new EnumMap<>(Color.class);
  // Each color has specific entry and home entry points on the board
  private static final Map<Color, Integer> ENTRY_POINTS = Map.of(
      Color.RED, 0,
      Color.BLUE, 13,
      Color.YELLOW, 26,
      Color.GREEN, 39
  );

  private static final Map<Color, Integer> HOME_ENTRY_POINTS = Map.of(
      Color.RED, 50,
      Color.BLUE, 11,
      Color.YELLOW, 24,
      Color.GREEN, 37
  );

  public int getEntryPoint(Color color) {
    return ENTRY_POINTS.get(color);
  }

  public int getHomeEntryPoint(Color color) {
    return HOME_ENTRY_POINTS.get(color);
  }

  public void initialiseHomePath(Color color,
      BiConsumer<Color, List<Cell>> homePathConsumer) {
    List<Cell> homePathCells = new ArrayList<>();
    int baseId = 100 + color.ordinal() * 10; // Use different ID range for home path cells
    for (int i = 0; i < 6; i++) {
      Cell cell = new Cell(baseId + i, color);
      CellType cellType = i == 5 ? CellType.HOME : CellType.HOME_PATH;
      cell.setCellType(cellType);
      homePathCells.add(cell);
      if (i > 0) {
        homePathCells.get(i - 1).setNextCell(cell);
      }
    }
    homePathConsumer.accept(color, homePathCells);
  }

  public void initialiseMainPath(List<Cell> mainPath) {
    // First create all cells
    for (int i = 0; i < 52; i++) {
      Cell cell = new Cell(i, null);
      cell.setCellType(CellType.NORMAL);
      mainPath.add(cell);
    }

    // Set up next cell connections
    for (int i = 0; i < 52; i++) {
      if (i < 51) {
        mainPath.get(i).setNextCell(mainPath.get(i + 1));
      }
    }
    // Complete the circle
    mainPath.get(51).setNextCell(mainPath.get(0));

    // Set safe cells
    for (int i = 0; i < 52; i += 13) {
      mainPath.get(i).setCellType(CellType.SAFE);
    }
  }

  public void connectHomePaths(LudoBoard board) {
    for (Color color : Color.values()) {
      int homeEntryIndex = getHomeEntryPoint(color);
      Cell mainPathEntry = board.getMainPathCellByIndex(homeEntryIndex);
      List<Cell> homePath = board.getHomePath(color);
      if (!homePath.isEmpty()) {
        mainPathEntry.setHomeEntry(homePath.get(0));
      }
    }
  }
}
