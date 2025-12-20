package com.ezbattlemap.dualscreen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Panel displaying the image library with thumbnails and management controls.
 */
public class ImageLibraryPanel extends JPanel {
    private final ImageLibrary library;
    private final JPanel thumbnailGrid;
    private final JComboBox<String> categoryFilter;
    private final JTextField searchField;
    private final JComboBox<String> libraryTypeSelector;
    private ImageSelectionListener selectionListener;
    private TokenDragListener tokenDragListener;
    private String selectedImageId;
    private ImageMetadata.LibraryType currentLibraryType;

    public ImageLibraryPanel(ImageLibrary library) {
        this.library = library;
        this.thumbnailGrid = new JPanel();
        this.currentLibraryType = ImageMetadata.LibraryType.MAP;

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("Image Library"));

        // Top panel with controls
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));

        // Library type selector (Maps vs Tokens)
        libraryTypeSelector = new JComboBox<>(new String[]{"Maps", "Tokens"});
        libraryTypeSelector.addActionListener(e -> {
            currentLibraryType = libraryTypeSelector.getSelectedIndex() == 0 ?
                ImageMetadata.LibraryType.MAP : ImageMetadata.LibraryType.TOKEN;
            refreshCategories();
            refreshThumbnails();
        });
        JPanel typePanel = new JPanel(new BorderLayout(5, 5));
        typePanel.add(new JLabel("Library:"), BorderLayout.WEST);
        typePanel.add(libraryTypeSelector, BorderLayout.CENTER);

        // Search field
        searchField = new JTextField();
        searchField.addActionListener(e -> refreshThumbnails());
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.add(new JLabel("Search:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        JButton searchBtn = new JButton("Go");
        searchBtn.addActionListener(e -> refreshThumbnails());
        searchPanel.add(searchBtn, BorderLayout.EAST);

        // Category filter
        categoryFilter = new JComboBox<>();
        categoryFilter.addActionListener(e -> refreshThumbnails());
        JPanel categoryPanel = new JPanel(new BorderLayout(5, 5));
        categoryPanel.add(new JLabel("Category:"), BorderLayout.WEST);
        categoryPanel.add(categoryFilter, BorderLayout.CENTER);

        // Combine all filters
        JPanel filterPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        filterPanel.add(typePanel);
        filterPanel.add(searchPanel);
        filterPanel.add(categoryPanel);
        topPanel.add(filterPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        JButton addButton = new JButton("Add Image");
        addButton.addActionListener(e -> addImage());
        JButton editButton = new JButton("Edit");
        editButton.addActionListener(e -> editSelectedImage());
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteSelectedImage());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // Thumbnail grid with scroll pane
        thumbnailGrid.setLayout(new GridLayout(0, 2, 5, 5));
        thumbnailGrid.setBackground(new Color(50, 50, 50));
        JScrollPane scrollPane = new JScrollPane(thumbnailGrid);
        scrollPane.setPreferredSize(new Dimension(340, 400));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Initialize
        refreshCategories();
        refreshThumbnails();
    }

    /**
     * Refresh the category dropdown.
     */
    private void refreshCategories() {
        String selected = (String) categoryFilter.getSelectedItem();
        categoryFilter.removeAllItems();
        categoryFilter.addItem("All Categories");

        Set<String> categories = library.getCategoriesByType(currentLibraryType);
        List<String> sortedCategories = new ArrayList<>(categories);
        Collections.sort(sortedCategories);

        for (String category : sortedCategories) {
            categoryFilter.addItem(category);
        }

        if (selected != null && categories.contains(selected)) {
            categoryFilter.setSelectedItem(selected);
        }
    }

    /**
     * Refresh the thumbnail display.
     */
    public void refreshThumbnails() {
        thumbnailGrid.removeAll();

        // Get filtered image IDs
        List<String> imageIds = getFilteredImageIds();

        // Sort by display name
        imageIds.sort((id1, id2) -> {
            ImageMetadata m1 = library.getMetadata(id1);
            ImageMetadata m2 = library.getMetadata(id2);
            return m1.getDisplayName().compareToIgnoreCase(m2.getDisplayName());
        });

        // Create thumbnail panels
        for (String id : imageIds) {
            ImageMetadata meta = library.getMetadata(id);
            if (meta != null) {
                ThumbnailPanel thumbPanel = new ThumbnailPanel(id, meta);
                thumbnailGrid.add(thumbPanel);
            }
        }

        thumbnailGrid.revalidate();
        thumbnailGrid.repaint();
    }

    /**
     * Get image IDs based on current filter settings.
     */
    private List<String> getFilteredImageIds() {
        String category = (String) categoryFilter.getSelectedItem();
        String searchQuery = searchField.getText().trim();

        List<String> imageIds;

        // Filter by library type and category
        if (category == null || category.equals("All Categories")) {
            imageIds = library.getImageIdsByType(currentLibraryType);
        } else {
            imageIds = library.getImageIdsByCategoryAndType(category, currentLibraryType);
        }

        // Filter by search query
        if (!searchQuery.isEmpty()) {
            List<String> searchResults = library.searchImages(searchQuery);
            imageIds.retainAll(searchResults);
        }

        return imageIds;
    }

    /**
     * Add a new image to the library.
     */
    private void addImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                String name = f.getName().toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                       name.endsWith(".png") || name.endsWith(".gif") ||
                       name.endsWith(".bmp");
            }

            @Override
            public String getDescription() {
                return "Image files (*.jpg, *.png, *.gif, *.bmp)";
            }
        });

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = fileChooser.getSelectedFile();
                ImageMetadata meta = library.addImage(selectedFile, currentLibraryType);

                // Show edit dialog for the new image
                showEditDialog(meta);

                refreshCategories();
                refreshThumbnails();

                JOptionPane.showMessageDialog(this,
                        "Image added successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error adding image: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Mass import multiple images to the library.
     */
    public void massImportImages() {
        massImportWithType(currentLibraryType);
    }

    /**
     * Mass import multiple maps to the Maps library.
     */
    public void massImportMaps() {
        massImportWithType(ImageMetadata.LibraryType.MAP);
    }

    /**
     * Mass import multiple tokens to the Tokens library.
     */
    public void massImportTokens() {
        massImportWithType(ImageMetadata.LibraryType.TOKEN);
    }

    /**
     * Mass import helper method.
     */
    private void massImportWithType(ImageMetadata.LibraryType libraryType) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                String name = f.getName().toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                       name.endsWith(".png") || name.endsWith(".gif") ||
                       name.endsWith(".bmp");
            }

            @Override
            public String getDescription() {
                return "Image files (*.jpg, *.png, *.gif, *.bmp)";
            }
        });

        String libraryName = (libraryType == ImageMetadata.LibraryType.MAP) ? "Maps" : "Tokens";
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            int successCount = 0;
            int failCount = 0;
            StringBuilder errors = new StringBuilder();

            for (File file : selectedFiles) {
                try {
                    library.addImage(file, libraryType);
                    successCount++;
                } catch (IOException ex) {
                    failCount++;
                    errors.append(file.getName()).append(": ").append(ex.getMessage()).append("\n");
                }
            }

            refreshCategories();
            refreshThumbnails();

            // Show results
            String message = String.format("Import to %s library complete!\n\nSuccessfully imported: %d\nFailed: %d",
                    libraryName, successCount, failCount);
            if (failCount > 0) {
                message += "\n\nErrors:\n" + errors.toString();
            }

            JOptionPane.showMessageDialog(this,
                    message,
                    "Mass Import Results - " + libraryName,
                    failCount > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Edit the selected image metadata.
     */
    private void editSelectedImage() {
        if (selectedImageId == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select an image first.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        ImageMetadata meta = library.getMetadata(selectedImageId);
        if (meta != null) {
            showEditDialog(meta);
            refreshCategories();
            refreshThumbnails();
        }
    }

    /**
     * Show the edit dialog for image metadata.
     */
    private void showEditDialog(ImageMetadata meta) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Image", true);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Display name
        formPanel.add(new JLabel("Name:"));
        JTextField nameField = new JTextField(meta.getDisplayName());
        formPanel.add(nameField);

        // Category
        formPanel.add(new JLabel("Category:"));
        JComboBox<String> categoryCombo = new JComboBox<>();
        for (String cat : library.getAllCategories()) {
            categoryCombo.addItem(cat);
        }
        categoryCombo.setEditable(true);
        categoryCombo.setSelectedItem(meta.getCategory());
        formPanel.add(categoryCombo);

        // Tags
        formPanel.add(new JLabel("Tags (comma-separated):"));
        JTextField tagsField = new JTextField(String.join(", ", meta.getTags()));
        formPanel.add(tagsField);

        // Notes
        formPanel.add(new JLabel("Notes:"));
        JTextField notesField = new JTextField(meta.getNotes());
        formPanel.add(notesField);

        dialog.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            meta.setDisplayName(nameField.getText().trim());
            meta.setCategory((String) categoryCombo.getSelectedItem());
            meta.setNotes(notesField.getText().trim());

            // Parse tags
            String[] tagArray = tagsField.getText().split(",");
            Set<String> tags = new HashSet<>();
            for (String tag : tagArray) {
                String trimmed = tag.trim();
                if (!trimmed.isEmpty()) {
                    tags.add(trimmed);
                }
            }
            meta.setTags(tags);

            library.updateMetadata(meta.getId(), meta);
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Delete the selected image.
     */
    private void deleteSelectedImage() {
        if (selectedImageId == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select an image first.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        ImageMetadata meta = library.getMetadata(selectedImageId);
        if (meta == null) {
            return;
        }

        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete '" + meta.getDisplayName() + "'?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            library.deleteImage(selectedImageId);
            selectedImageId = null;
            refreshCategories();
            refreshThumbnails();
        }
    }

    /**
     * Set the selection listener.
     */
    public void setSelectionListener(ImageSelectionListener listener) {
        this.selectionListener = listener;
    }

    /**
     * Set the token drag listener.
     */
    public void setTokenDragListener(TokenDragListener listener) {
        this.tokenDragListener = listener;
    }

    /**
     * Interface for image selection events.
     */
    public interface ImageSelectionListener {
        void onImageSelected(String imageId, BufferedImage image);
    }

    /**
     * Interface for token drag events.
     */
    public interface TokenDragListener {
        void onTokenDragStart(String imageId, BufferedImage tokenImage);
        void onTokenPlace(String imageId, BufferedImage tokenImage, int gridX, int gridY);
    }

    /**
     * Panel representing a single thumbnail.
     */
    private class ThumbnailPanel extends JPanel {
        private final String imageId;
        private final ImageMetadata metadata;
        private boolean isSelected = false;

        public ThumbnailPanel(String imageId, ImageMetadata metadata) {
            this.imageId = imageId;
            this.metadata = metadata;

            setLayout(new BorderLayout());
            setBackground(new Color(60, 60, 60));
            setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 2));
            setPreferredSize(new Dimension(160, 180));

            // Thumbnail image
            BufferedImage thumbnail = library.getThumbnail(imageId);
            JLabel imageLabel = new JLabel();
            if (thumbnail != null) {
                imageLabel.setIcon(new ImageIcon(thumbnail));
            }
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
            add(imageLabel, BorderLayout.CENTER);

            // Name label
            JLabel nameLabel = new JLabel(metadata.getDisplayName(), JLabel.CENTER);
            nameLabel.setForeground(Color.WHITE);
            nameLabel.setFont(nameLabel.getFont().deriveFont(11f));
            add(nameLabel, BorderLayout.SOUTH);

            // Mouse listener for selection and drag
            MouseAdapter mouseHandler = new MouseAdapter() {
                private Point pressPoint;
                private boolean isDragging = false;

                @Override
                public void mousePressed(MouseEvent e) {
                    pressPoint = e.getPoint();
                    isDragging = false;
                    setSelected(true);
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (pressPoint != null && !isDragging) {
                        int dx = (int) Math.abs(e.getX() - pressPoint.getX());
                        int dy = (int) Math.abs(e.getY() - pressPoint.getY());

                        // Start drag if moved more than 5 pixels
                        if (dx > 5 || dy > 5) {
                            isDragging = true;
                            // Only tokens can be dragged
                            if (metadata.getLibraryType() == ImageMetadata.LibraryType.TOKEN) {
                                startTokenDrag();
                            }
                        }
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    pressPoint = null;
                    isDragging = false;
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && !isDragging) {
                        // Double-click behavior depends on type
                        if (metadata.getLibraryType() == ImageMetadata.LibraryType.MAP) {
                            // Maps: load as background
                            loadAndSelectImage();
                        } else {
                            // Tokens: place on map at center
                            placeTokenAtCenter();
                        }
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!isSelected) {
                        setBorder(BorderFactory.createLineBorder(new Color(120, 120, 120), 2));
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!isSelected) {
                        setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 2));
                    }
                }
            };

            addMouseListener(mouseHandler);
            addMouseMotionListener(mouseHandler);

            // Show tooltip with metadata
            setToolTipText(buildTooltip());
        }

        private void setSelected(boolean selected) {
            // Deselect other thumbnails
            if (selected) {
                for (Component comp : thumbnailGrid.getComponents()) {
                    if (comp instanceof ThumbnailPanel) {
                        ((ThumbnailPanel) comp).isSelected = false;
                        ((ThumbnailPanel) comp).updateBorder();
                    }
                }
                selectedImageId = imageId;
            }

            isSelected = selected;
            updateBorder();
        }

        private void updateBorder() {
            if (isSelected) {
                setBorder(BorderFactory.createLineBorder(new Color(100, 150, 255), 3));
            } else {
                setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 2));
            }
        }

        private void loadAndSelectImage() {
            try {
                BufferedImage image = library.loadImage(imageId);
                if (selectionListener != null) {
                    selectionListener.onImageSelected(imageId, image);
                }
                setSelected(true);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(ImageLibraryPanel.this,
                        "Error loading image: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        private void startTokenDrag() {
            // Change cursor to indicate dragging
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // Notify listener that drag has started
            if (tokenDragListener != null) {
                try {
                    BufferedImage tokenImage = library.loadImage(imageId);
                    tokenDragListener.onTokenDragStart(imageId, tokenImage);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(ImageLibraryPanel.this,
                            "Error loading token image: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

            // Reset cursor when drag ends
            setCursor(Cursor.getDefaultCursor());
        }

        private void placeTokenAtCenter() {
            // Double-click on token places it at center of map
            if (tokenDragListener != null) {
                try {
                    BufferedImage tokenImage = library.loadImage(imageId);
                    tokenDragListener.onTokenDragStart(imageId, tokenImage);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(ImageLibraryPanel.this,
                            "Error loading token image: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private String buildTooltip() {
            StringBuilder sb = new StringBuilder("<html>");
            sb.append("<b>").append(metadata.getDisplayName()).append("</b><br>");
            sb.append("Category: ").append(metadata.getCategory()).append("<br>");

            if (!metadata.getTags().isEmpty()) {
                sb.append("Tags: ").append(String.join(", ", metadata.getTags())).append("<br>");
            }

            if (!metadata.getNotes().isEmpty()) {
                sb.append("Notes: ").append(metadata.getNotes()).append("<br>");
            }

            sb.append("</html>");
            return sb.toString();
        }
    }
}
