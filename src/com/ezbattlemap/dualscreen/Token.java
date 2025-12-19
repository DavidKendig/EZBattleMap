package com.ezbattlemap.dualscreen;

import java.awt.image.BufferedImage;
import java.io.Serializable;

/**
 * Represents a token placed on the map grid.
 */
public class Token implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;                  // Unique token instance ID
    private String imageId;             // Reference to library image
    private int gridX;                  // Grid cell X position
    private int gridY;                  // Grid cell Y position
    private int gridWidth;              // Token width in grid squares (default 1)
    private int gridHeight;             // Token height in grid squares (default 1)
    private transient BufferedImage cachedImage;  // Cached image (not serialized)

    public Token(String id, String imageId, int gridX, int gridY) {
        this.id = id;
        this.imageId = imageId;
        this.gridX = gridX;
        this.gridY = gridY;
        this.gridWidth = 1;
        this.gridHeight = 1;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getImageId() {
        return imageId;
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public BufferedImage getCachedImage() {
        return cachedImage;
    }

    // Setters
    public void setGridX(int gridX) {
        this.gridX = gridX;
    }

    public void setGridY(int gridY) {
        this.gridY = gridY;
    }

    public void setGridWidth(int gridWidth) {
        this.gridWidth = gridWidth;
    }

    public void setGridHeight(int gridHeight) {
        this.gridHeight = gridHeight;
    }

    public void setCachedImage(BufferedImage cachedImage) {
        this.cachedImage = cachedImage;
    }

    /**
     * Check if this token overlaps with the given grid cell.
     */
    public boolean overlapsCell(int cellX, int cellY) {
        return cellX >= gridX && cellX < gridX + gridWidth &&
               cellY >= gridY && cellY < gridY + gridHeight;
    }

    @Override
    public String toString() {
        return "Token[" + id + " at (" + gridX + "," + gridY + ") size " + gridWidth + "x" + gridHeight + "]";
    }
}
