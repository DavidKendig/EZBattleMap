package com.ezbattlemap.dualscreen;

import java.util.*;

/**
 * Manages tokens placed on the battlemap.
 */
public class TokenOverlay {
    private final Map<String, Token> tokens;
    private int nextTokenId;

    public TokenOverlay() {
        this.tokens = new LinkedHashMap<>();  // Preserve insertion order for rendering
        this.nextTokenId = 1;
    }

    /**
     * Add a token to the overlay.
     */
    public Token addToken(String imageId, int gridX, int gridY) {
        String tokenId = "token_" + nextTokenId++;
        Token token = new Token(tokenId, imageId, gridX, gridY);
        tokens.put(tokenId, token);
        return token;
    }

    /**
     * Remove a token from the overlay.
     */
    public void removeToken(String tokenId) {
        tokens.remove(tokenId);
    }

    /**
     * Get a token by ID.
     */
    public Token getToken(String tokenId) {
        return tokens.get(tokenId);
    }

    /**
     * Get all tokens.
     */
    public Collection<Token> getAllTokens() {
        return new ArrayList<>(tokens.values());
    }

    /**
     * Find token at a specific grid position (returns topmost token).
     */
    public Token getTokenAtPosition(int gridX, int gridY) {
        // Iterate in reverse order to get topmost token
        List<Token> tokenList = new ArrayList<>(tokens.values());
        for (int i = tokenList.size() - 1; i >= 0; i--) {
            Token token = tokenList.get(i);
            if (token.overlapsCell(gridX, gridY)) {
                return token;
            }
        }
        return null;
    }

    /**
     * Move a token to a new grid position.
     */
    public void moveToken(String tokenId, int newGridX, int newGridY) {
        Token token = tokens.get(tokenId);
        if (token != null) {
            token.setGridX(newGridX);
            token.setGridY(newGridY);
        }
    }

    /**
     * Change the size of a token.
     */
    public void resizeToken(String tokenId, int gridWidth, int gridHeight) {
        Token token = tokens.get(tokenId);
        if (token != null) {
            token.setGridWidth(gridWidth);
            token.setGridHeight(gridHeight);
        }
    }

    /**
     * Clear all tokens.
     */
    public void clearAll() {
        tokens.clear();
    }

    /**
     * Get number of tokens.
     */
    public int getTokenCount() {
        return tokens.size();
    }
}
