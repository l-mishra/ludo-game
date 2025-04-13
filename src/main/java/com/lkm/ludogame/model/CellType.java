package com.lkm.ludogame.model;

public enum CellType {
    START, // Starting cell for each color
    NORMAL, // REGULAR CELLS
    SAFE, // Safe Spots where pieces can't be captured
    HOME_PATH, // Colored path to home
    HOME // Final destinations
}
