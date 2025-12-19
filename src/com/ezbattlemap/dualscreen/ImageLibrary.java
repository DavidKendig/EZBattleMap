package com.ezbattlemap.dualscreen;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

/**
 * Manages the image library including storage, metadata, and retrieval.
 */
public class ImageLibrary {
    private static final String LIBRARY_DIR_NAME = ".ezbattlemap";
    private static final String MAPS_DIR_NAME = "maps";
    private static final String TOKENS_DIR_NAME = "tokens";
    private static final String METADATA_FILE_NAME = "library.dat";
    private static final String THUMBNAILS_DIR_NAME = "thumbnails";
    private static final int THUMBNAIL_SIZE = 150;

    private final File libraryRoot;
    private final File mapsDir;
    private final File tokensDir;
    private final File thumbnailsDir;
    private final File metadataFile;
    private final Map<String, ImageMetadata> metadata;
    private final Map<String, BufferedImage> thumbnailCache;

    public ImageLibrary() throws IOException {
        // Set up directory structure in user home
        String userHome = System.getProperty("user.home");
        this.libraryRoot = new File(userHome, LIBRARY_DIR_NAME);
        this.mapsDir = new File(libraryRoot, MAPS_DIR_NAME);
        this.tokensDir = new File(libraryRoot, TOKENS_DIR_NAME);
        this.thumbnailsDir = new File(libraryRoot, THUMBNAILS_DIR_NAME);
        this.metadataFile = new File(libraryRoot, METADATA_FILE_NAME);
        this.metadata = new HashMap<>();
        this.thumbnailCache = new HashMap<>();

        // Create directories if they don't exist
        createDirectories();

        // Load existing metadata
        loadMetadata();
    }

    private void createDirectories() throws IOException {
        if (!libraryRoot.exists()) {
            libraryRoot.mkdirs();
        }
        if (!mapsDir.exists()) {
            mapsDir.mkdirs();
        }
        if (!tokensDir.exists()) {
            tokensDir.mkdirs();
        }
        if (!thumbnailsDir.exists()) {
            thumbnailsDir.mkdirs();
        }
    }

    /**
     * Load metadata from disk.
     */
    @SuppressWarnings("unchecked")
    private void loadMetadata() {
        if (!metadataFile.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(metadataFile))) {
            Map<String, ImageMetadata> loaded = (Map<String, ImageMetadata>) ois.readObject();
            metadata.putAll(loaded);
        } catch (Exception e) {
            System.err.println("Error loading metadata: " + e.getMessage());
        }
    }

    /**
     * Save metadata to disk.
     */
    private void saveMetadata() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(metadataFile))) {
            oos.writeObject(metadata);
        } catch (Exception e) {
            System.err.println("Error saving metadata: " + e.getMessage());
        }
    }

    /**
     * Add an image to the library by copying it from the source file.
     */
    public ImageMetadata addImage(File sourceFile, ImageMetadata.LibraryType libraryType) throws IOException {
        // Generate unique ID
        String baseName = getBaseName(sourceFile.getName());
        String extension = getExtension(sourceFile.getName());
        String id = generateUniqueId(baseName);

        // Determine target directory
        File targetDir = (libraryType == ImageMetadata.LibraryType.TOKEN) ? tokensDir : mapsDir;

        // Copy file to library
        String fileName = id + "." + extension;
        File destFile = new File(targetDir, fileName);
        Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Create metadata
        ImageMetadata meta = new ImageMetadata(id, fileName, libraryType);
        meta.setDisplayName(baseName);
        metadata.put(id, meta);

        // Generate thumbnail
        generateThumbnail(destFile, id);

        // Save metadata
        saveMetadata();

        return meta;
    }

    /**
     * Add an image to the library (backward compatibility - defaults to MAP).
     */
    public ImageMetadata addImage(File sourceFile) throws IOException {
        return addImage(sourceFile, ImageMetadata.LibraryType.MAP);
    }

    /**
     * Get the directory for a specific library type.
     */
    private File getDirectoryForType(ImageMetadata.LibraryType type) {
        return (type == ImageMetadata.LibraryType.TOKEN) ? tokensDir : mapsDir;
    }

    /**
     * Generate a unique ID based on the base name.
     */
    private String generateUniqueId(String baseName) {
        String id = baseName;
        int counter = 1;
        while (metadata.containsKey(id)) {
            id = baseName + "_" + counter;
            counter++;
        }
        return id;
    }

    /**
     * Generate thumbnail for an image.
     */
    private void generateThumbnail(File imageFile, String id) {
        try {
            BufferedImage original = ImageIO.read(imageFile);
            if (original == null) {
                return;
            }

            // Calculate thumbnail dimensions maintaining aspect ratio
            int width = original.getWidth();
            int height = original.getHeight();
            double scale = Math.min((double) THUMBNAIL_SIZE / width, (double) THUMBNAIL_SIZE / height);
            int thumbWidth = (int) (width * scale);
            int thumbHeight = (int) (height * scale);

            // Create thumbnail
            BufferedImage thumbnail = new BufferedImage(THUMBNAIL_SIZE, THUMBNAIL_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = thumbnail.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // Center the image
            int x = (THUMBNAIL_SIZE - thumbWidth) / 2;
            int y = (THUMBNAIL_SIZE - thumbHeight) / 2;

            // Fill background with dark color
            g2d.setColor(new Color(40, 40, 40));
            g2d.fillRect(0, 0, THUMBNAIL_SIZE, THUMBNAIL_SIZE);

            // Draw scaled image
            g2d.drawImage(original, x, y, thumbWidth, thumbHeight, null);
            g2d.dispose();

            // Save thumbnail
            File thumbFile = new File(thumbnailsDir, id + ".png");
            ImageIO.write(thumbnail, "PNG", thumbFile);

            // Cache thumbnail
            thumbnailCache.put(id, thumbnail);

        } catch (Exception e) {
            System.err.println("Error generating thumbnail: " + e.getMessage());
        }
    }

    /**
     * Get thumbnail for an image.
     */
    public BufferedImage getThumbnail(String id) {
        // Check cache first
        if (thumbnailCache.containsKey(id)) {
            return thumbnailCache.get(id);
        }

        // Load from disk
        File thumbFile = new File(thumbnailsDir, id + ".png");
        if (thumbFile.exists()) {
            try {
                BufferedImage thumb = ImageIO.read(thumbFile);
                thumbnailCache.put(id, thumb);
                return thumb;
            } catch (Exception e) {
                System.err.println("Error loading thumbnail: " + e.getMessage());
            }
        }

        return null;
    }

    /**
     * Load full image from library.
     */
    public BufferedImage loadImage(String id) throws IOException {
        ImageMetadata meta = metadata.get(id);
        if (meta == null) {
            throw new IOException("Image not found: " + id);
        }

        File imageDir = getDirectoryForType(meta.getLibraryType());
        File imageFile = new File(imageDir, meta.getFileName());
        if (!imageFile.exists()) {
            throw new IOException("Image file not found: " + imageFile.getAbsolutePath());
        }

        return ImageIO.read(imageFile);
    }

    /**
     * Delete an image from the library.
     */
    public void deleteImage(String id) {
        ImageMetadata meta = metadata.remove(id);
        if (meta == null) {
            return;
        }

        // Delete image file
        File imageDir = getDirectoryForType(meta.getLibraryType());
        File imageFile = new File(imageDir, meta.getFileName());
        if (imageFile.exists()) {
            imageFile.delete();
        }

        // Delete thumbnail
        File thumbFile = new File(thumbnailsDir, id + ".png");
        if (thumbFile.exists()) {
            thumbFile.delete();
        }

        // Remove from cache
        thumbnailCache.remove(id);

        // Save metadata
        saveMetadata();
    }

    /**
     * Update metadata for an image.
     */
    public void updateMetadata(String id, ImageMetadata updatedMeta) {
        metadata.put(id, updatedMeta);
        saveMetadata();
    }

    /**
     * Get metadata for an image.
     */
    public ImageMetadata getMetadata(String id) {
        return metadata.get(id);
    }

    /**
     * Get all image IDs.
     */
    public List<String> getAllImageIds() {
        return new ArrayList<>(metadata.keySet());
    }

    /**
     * Get all image IDs for a specific library type.
     */
    public List<String> getImageIdsByType(ImageMetadata.LibraryType type) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, ImageMetadata> entry : metadata.entrySet()) {
            if (entry.getValue().getLibraryType() == type) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * Get all image IDs in a specific category.
     */
    public List<String> getImageIdsByCategory(String category) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, ImageMetadata> entry : metadata.entrySet()) {
            if (entry.getValue().getCategory().equals(category)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * Get all image IDs in a specific category and library type.
     */
    public List<String> getImageIdsByCategoryAndType(String category, ImageMetadata.LibraryType type) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, ImageMetadata> entry : metadata.entrySet()) {
            ImageMetadata meta = entry.getValue();
            if (meta.getCategory().equals(category) && meta.getLibraryType() == type) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * Get all categories.
     */
    public Set<String> getAllCategories() {
        Set<String> categories = new HashSet<>();
        for (ImageMetadata meta : metadata.values()) {
            categories.add(meta.getCategory());
        }
        return categories;
    }

    /**
     * Get all categories for a specific library type.
     */
    public Set<String> getCategoriesByType(ImageMetadata.LibraryType type) {
        Set<String> categories = new HashSet<>();
        for (ImageMetadata meta : metadata.values()) {
            if (meta.getLibraryType() == type) {
                categories.add(meta.getCategory());
            }
        }
        return categories;
    }

    /**
     * Search images by query.
     */
    public List<String> searchImages(String query) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, ImageMetadata> entry : metadata.entrySet()) {
            if (entry.getValue().matchesSearch(query)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * Get base name without extension.
     */
    private String getBaseName(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }

    /**
     * Get file extension.
     */
    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1);
        }
        return "png";
    }

    /**
     * Get the library root directory.
     */
    public File getLibraryRoot() {
        return libraryRoot;
    }
}
