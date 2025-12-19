package com.ezbattlemap.dualscreen;

import java.io.*;
import java.util.*;

/**
 * Stores metadata information for library images including name, category, tags, and notes.
 */
public class ImageMetadata implements Serializable {
    private static final long serialVersionUID = 2L;

    public enum LibraryType {
        MAP, TOKEN
    }

    private String id;              // Unique identifier (filename without extension)
    private String displayName;     // User-friendly name
    private String category;        // Category/folder (e.g., "Maps", "Tokens")
    private Set<String> tags;       // Search tags
    private String notes;           // User notes/description
    private String fileName;        // Actual file name on disk
    private long dateAdded;         // Timestamp when added
    private long lastModified;      // Last modification timestamp
    private LibraryType libraryType; // MAP or TOKEN

    public ImageMetadata(String id, String fileName, LibraryType libraryType) {
        this.id = id;
        this.fileName = fileName;
        this.libraryType = libraryType;
        this.displayName = id;
        this.category = "Uncategorized";
        this.tags = new HashSet<>();
        this.notes = "";
        this.dateAdded = System.currentTimeMillis();
        this.lastModified = this.dateAdded;
    }

    // Backward compatibility constructor
    public ImageMetadata(String id, String fileName) {
        this(id, fileName, LibraryType.MAP);
    }

    // Getters
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getCategory() { return category; }
    public Set<String> getTags() { return new HashSet<>(tags); }
    public String getNotes() { return notes; }
    public String getFileName() { return fileName; }
    public long getDateAdded() { return dateAdded; }
    public long getLastModified() { return lastModified; }
    public LibraryType getLibraryType() { return libraryType; }

    // Setters
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        this.lastModified = System.currentTimeMillis();
    }

    public void setCategory(String category) {
        this.category = category;
        this.lastModified = System.currentTimeMillis();
    }

    public void setNotes(String notes) {
        this.notes = notes;
        this.lastModified = System.currentTimeMillis();
    }

    public void addTag(String tag) {
        this.tags.add(tag.toLowerCase().trim());
        this.lastModified = System.currentTimeMillis();
    }

    public void removeTag(String tag) {
        this.tags.remove(tag.toLowerCase().trim());
        this.lastModified = System.currentTimeMillis();
    }

    public void setTags(Set<String> tags) {
        this.tags = new HashSet<>();
        for (String tag : tags) {
            this.tags.add(tag.toLowerCase().trim());
        }
        this.lastModified = System.currentTimeMillis();
    }

    /**
     * Check if this metadata matches a search query.
     */
    public boolean matchesSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }

        String lowerQuery = query.toLowerCase();

        // Search in display name
        if (displayName.toLowerCase().contains(lowerQuery)) {
            return true;
        }

        // Search in category
        if (category.toLowerCase().contains(lowerQuery)) {
            return true;
        }

        // Search in tags
        for (String tag : tags) {
            if (tag.contains(lowerQuery)) {
                return true;
            }
        }

        // Search in notes
        if (notes.toLowerCase().contains(lowerQuery)) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return displayName + " (" + category + ")";
    }
}
