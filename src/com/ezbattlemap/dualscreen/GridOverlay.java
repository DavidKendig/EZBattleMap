package com.ezbattlemap.dualscreen;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the grid overlay and cell selection state
 */
public class GridOverlay {
    private int squareSize = 100; // pixel size of each square
    private boolean[][] selectedCells;
    private int imageWidth;
    private int imageHeight;
    private int gridCols;
    private int gridRows;

    public GridOverlay() {
        updateGridDimensions();
    }

    public void setSquareSize(int size) {
        if (size != squareSize && size > 0) {
            squareSize = size;
            updateGridDimensions();
        }
    }

    public int getSquareSize() {
        return squareSize;
    }

    public int getGridSize() {
        return Math.max(gridCols, gridRows);
    }

    public int getGridCols() {
        return gridCols;
    }

    public int getGridRows() {
        return gridRows;
    }

    public void setImageDimensions(int width, int height) {
        this.imageWidth = width;
        this.imageHeight = height;
        updateGridDimensions();
    }

    private void updateGridDimensions() {
        if (imageWidth > 0 && imageHeight > 0 && squareSize > 0) {
            gridCols = (int) Math.ceil((double) imageWidth / squareSize);
            gridRows = (int) Math.ceil((double) imageHeight / squareSize);
            selectedCells = new boolean[gridRows][gridCols];
        } else {
            gridCols = 0;
            gridRows = 0;
            selectedCells = new boolean[0][0];
        }
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void toggleCell(int x, int y) {
        if (x >= 0 && x < gridCols && y >= 0 && y < gridRows) {
            selectedCells[y][x] = !selectedCells[y][x];
        }
    }

    public void selectCell(int x, int y, boolean selected) {
        if (x >= 0 && x < gridCols && y >= 0 && y < gridRows) {
            selectedCells[y][x] = selected;
        }
    }

    public boolean isCellSelected(int x, int y) {
        if (x >= 0 && x < gridCols && y >= 0 && y < gridRows) {
            return selectedCells[y][x];
        }
        return false;
    }

    public boolean[][] getSelectedCells() {
        return selectedCells;
    }

    public void clearSelection() {
        for (int y = 0; y < gridRows; y++) {
            for (int x = 0; x < gridCols; x++) {
                selectedCells[y][x] = false;
            }
        }
    }

    public Rectangle getSelectedBounds() {
        int minX = gridCols;
        int minY = gridRows;
        int maxX = -1;
        int maxY = -1;

        for (int y = 0; y < gridRows; y++) {
            for (int x = 0; x < gridCols; x++) {
                if (selectedCells[y][x]) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        if (maxX < 0) {
            return null;
        }

        int x = minX * squareSize;
        int y = minY * squareSize;
        int width = ((maxX + 1) * squareSize) - x;
        int height = ((maxY + 1) * squareSize) - y;

        // Clamp to image boundaries
        width = Math.min(width, imageWidth - x);
        height = Math.min(height, imageHeight - y);

        return new Rectangle(x, y, width, height);
    }

    public boolean hasSelection() {
        for (int y = 0; y < gridRows; y++) {
            for (int x = 0; x < gridCols; x++) {
                if (selectedCells[y][x]) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Rectangle> getUnselectedCellRectangles() {
        List<Rectangle> unselected = new ArrayList<>();
        for (int y = 0; y < gridRows; y++) {
            for (int x = 0; x < gridCols; x++) {
                if (!selectedCells[y][x]) {
                    int cellX = x * squareSize;
                    int cellY = y * squareSize;
                    int cellW = Math.min(squareSize, imageWidth - cellX);
                    int cellH = Math.min(squareSize, imageHeight - cellY);
                    unselected.add(new Rectangle(cellX, cellY, cellW, cellH));
                }
            }
        }
        return unselected;
    }

    public Rectangle getCellRectangle(int x, int y) {
        if (x >= 0 && x < gridCols && y >= 0 && y < gridRows) {
            int cellX = x * squareSize;
            int cellY = y * squareSize;
            int cellW = Math.min(squareSize, imageWidth - cellX);
            int cellH = Math.min(squareSize, imageHeight - cellY);
            return new Rectangle(cellX, cellY, cellW, cellH);
        }
        return null;
    }
}
