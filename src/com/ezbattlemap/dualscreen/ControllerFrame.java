package com.ezbattlemap.dualscreen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * Controller frame for managing the image display and grid overlay
 */
public class ControllerFrame extends JFrame {
    private DualScreenImageApp app;
    private DisplayFrame displayFrame;
    private ImagePanel imagePanel;
    private JSpinner gridSizeSpinner;
    private JButton selectImageButton;
    private GridOverlay gridOverlay;
    private TokenOverlay tokenOverlay;
    private ImageLibrary library;
    private boolean dragSelectMode = false;
    private int lastCellX = -1;
    private int lastCellY = -1;
    private ImageLibraryPanel libraryPanel;
    private JComboBox<String> modeSelector;
    private boolean isTokenMode = false;
    private Token selectedToken = null;
    private String currentImageId = null;

    public ControllerFrame(DualScreenImageApp app, DisplayFrame displayFrame, ImageLibrary library) {
        this.app = app;
        this.displayFrame = displayFrame;
        this.library = library;

        setTitle("EZBattleMap - Controller");
        setSize(1400, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        gridOverlay = new GridOverlay();
        tokenOverlay = new TokenOverlay();
        imagePanel = new ImagePanel(gridOverlay, tokenOverlay, library);
        imagePanel.setController(this);

        JPanel controlPanel = createControlPanel();

        // Create image library panel on the left
        libraryPanel = new ImageLibraryPanel(library);
        libraryPanel.setSelectionListener((imageId, image) -> {
            currentImageId = imageId;
            setImage(image);
            displayFrame.setImage(image);

            // Load saved pixel size from metadata
            ImageMetadata metadata = library.getMetadata(imageId);
            if (metadata != null) {
                int savedPixelSize = metadata.getPixelSize();
                gridSizeSpinner.setValue(savedPixelSize);
                gridOverlay.setSquareSize(savedPixelSize);
                imagePanel.repaint();
                displayFrame.updateGridOverlay(gridOverlay);
            }
        });
        libraryPanel.setTokenDragListener(new ImageLibraryPanel.TokenDragListener() {
            @Override
            public void onTokenDragStart(String imageId, BufferedImage tokenImage) {
                // Store the dragged token information in the image panel
                imagePanel.setDraggingToken(imageId, tokenImage);
            }

            @Override
            public void onTokenPlace(String imageId, BufferedImage tokenImage, int gridX, int gridY) {
                // Place token at specific grid position
                Token token = tokenOverlay.addToken(imageId, gridX, gridY);
                token.setCachedImage(tokenImage);
                imagePanel.repaint();
                displayFrame.updateTokenOverlay(tokenOverlay);
                imagePanel.setDraggingToken(null, null);
            }
        });

        add(libraryPanel, BorderLayout.WEST);
        add(new JScrollPane(imagePanel), BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        MouseAdapter gridSelectionAdapter = new MouseAdapter() {
            private boolean isDragging = false;
            private Point dragStartPoint = null;

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && !e.isControlDown()) {
                    Point imagePoint = imagePanel.getImagePoint(e.getPoint());
                    if (imagePoint == null) return;

                    int cellX = imagePoint.x / gridOverlay.getSquareSize();
                    int cellY = imagePoint.y / gridOverlay.getSquareSize();

                    if (cellX < 0 || cellX >= gridOverlay.getGridCols() ||
                        cellY < 0 || cellY >= gridOverlay.getGridRows()) {
                        return;
                    }

                    // Check if we're dropping a token from the library
                    if (imagePanel.getDraggingTokenImageId() != null) {
                        // Place token at drop position
                        String imageId = imagePanel.getDraggingTokenImageId();
                        BufferedImage tokenImage = imagePanel.getDraggingTokenImage();
                        Token token = tokenOverlay.addToken(imageId, cellX, cellY);
                        token.setCachedImage(tokenImage);
                        imagePanel.setDraggingToken(null, null);
                        imagePanel.repaint();
                        displayFrame.updateTokenOverlay(tokenOverlay);
                        return;
                    }

                    if (ControllerFrame.this.isTokenMode) {
                        // Token mode: ONLY token selection and movement - NO grid changes
                        Token clickedToken = tokenOverlay.getTokenAtPosition(cellX, cellY);
                        if (clickedToken != null) {
                            ControllerFrame.this.selectedToken = clickedToken;
                            dragStartPoint = new Point(cellX, cellY);
                            isDragging = true;
                            imagePanel.repaint();
                        } else {
                            // Clicked on empty space - deselect token
                            ControllerFrame.this.selectedToken = null;
                            isDragging = false;
                            imagePanel.repaint();
                        }
                        // Do NOT modify grid overlay or viewport in token mode
                        return;
                    }

                    // Map mode: grid selection only
                    dragSelectMode = !gridOverlay.isCellSelected(cellX, cellY);
                    isDragging = true;
                    lastCellX = cellX;
                    lastCellY = cellY;

                    gridOverlay.selectCell(cellX, cellY, dragSelectMode);
                    imagePanel.repaint();
                    updateViewport();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging) {
                    Point imagePoint = imagePanel.getImagePoint(e.getPoint());
                    if (imagePoint != null) {
                        int cellX = imagePoint.x / gridOverlay.getSquareSize();
                        int cellY = imagePoint.y / gridOverlay.getSquareSize();

                        if (cellX >= 0 && cellX < gridOverlay.getGridCols() &&
                            cellY >= 0 && cellY < gridOverlay.getGridRows()) {

                            if (ControllerFrame.this.isTokenMode) {
                                // Token mode: only move selected token
                                if (ControllerFrame.this.selectedToken != null) {
                                    if (cellX != ControllerFrame.this.selectedToken.getGridX() || cellY != ControllerFrame.this.selectedToken.getGridY()) {
                                        ControllerFrame.this.selectedToken.setGridX(cellX);
                                        ControllerFrame.this.selectedToken.setGridY(cellY);
                                        imagePanel.repaint();
                                        displayFrame.updateTokenOverlay(tokenOverlay);
                                    }
                                }
                            } else {
                                // Map mode: grid selection only
                                if (cellX != lastCellX || cellY != lastCellY) {
                                    gridOverlay.selectCell(cellX, cellY, dragSelectMode);
                                    lastCellX = cellX;
                                    lastCellY = cellY;
                                    imagePanel.repaint();
                                    updateViewport();
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isDragging = false;
                dragStartPoint = null;
                lastCellX = -1;
                lastCellY = -1;
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // Right-click to delete token in token mode
                if (SwingUtilities.isRightMouseButton(e) && ControllerFrame.this.isTokenMode) {
                    Point imagePoint = imagePanel.getImagePoint(e.getPoint());
                    if (imagePoint != null) {
                        int cellX = imagePoint.x / gridOverlay.getSquareSize();
                        int cellY = imagePoint.y / gridOverlay.getSquareSize();

                        if (cellX >= 0 && cellX < gridOverlay.getGridCols() &&
                            cellY >= 0 && cellY < gridOverlay.getGridRows()) {

                            Token clickedToken = tokenOverlay.getTokenAtPosition(cellX, cellY);
                            if (clickedToken != null) {
                                int result = JOptionPane.showConfirmDialog(
                                    ControllerFrame.this,
                                    "Remove this token from the map?",
                                    "Remove Token",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.QUESTION_MESSAGE
                                );

                                if (result == JOptionPane.YES_OPTION) {
                                    tokenOverlay.removeToken(clickedToken.getId());
                                    if (ControllerFrame.this.selectedToken == clickedToken) {
                                        ControllerFrame.this.selectedToken = null;
                                    }
                                    imagePanel.repaint();
                                    displayFrame.updateTokenOverlay(tokenOverlay);
                                }
                            }
                        }
                    }
                }
            }
        };

        imagePanel.addMouseListener(gridSelectionAdapter);
        imagePanel.addMouseMotionListener(gridSelectionAdapter);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Mode selector
        JLabel modeLabel = new JLabel("Mode:");
        modeLabel.setFont(modeLabel.getFont().deriveFont(Font.BOLD, 14f));
        modeSelector = new JComboBox<>(new String[]{"Map Mode", "Token Mode"});
        modeSelector.setFont(modeSelector.getFont().deriveFont(Font.BOLD, 14f));

        // Use ItemListener for reliable selection change detection
        modeSelector.addItemListener(e -> {
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                isTokenMode = modeSelector.getSelectedIndex() == 1;
                selectedToken = null;
                imagePanel.repaint();
                // Update title to show current mode
                String modeText = isTokenMode ? "Token Mode" : "Map Mode";
                setTitle("EZBattleMap - Controller [" + modeText + "]");
            }
        });

        selectImageButton = new JButton("Select Image");
        selectImageButton.addActionListener(e -> app.promptForImage());

        JLabel gridLabel = new JLabel("Square Size (pixels):");
        gridSizeSpinner = new JSpinner(new SpinnerNumberModel(100, 1, 2000, 1));
        gridSizeSpinner.addChangeListener(e -> {
            int size = (Integer) gridSizeSpinner.getValue();
            gridOverlay.setSquareSize(size);
            imagePanel.repaint();
            displayFrame.updateGridOverlay(gridOverlay);

            // Save pixel size to metadata if an image is currently loaded from library
            if (currentImageId != null) {
                ImageMetadata metadata = library.getMetadata(currentImageId);
                if (metadata != null) {
                    metadata.setPixelSize(size);
                    library.updateMetadata(currentImageId, metadata);
                }
            }
        });

        JButton clearSelectionButton = new JButton("Clear Selection");
        clearSelectionButton.addActionListener(e -> {
            gridOverlay.clearSelection();
            imagePanel.repaint();
            displayFrame.updateViewport(null);
        });

        panel.add(modeLabel);
        panel.add(modeSelector);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(selectImageButton);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(gridLabel);
        panel.add(gridSizeSpinner);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(clearSelectionButton);

        return panel;
    }

    public void setImage(BufferedImage image) {
        imagePanel.setImage(image);
        gridOverlay.setImageDimensions(image.getWidth(), image.getHeight());
        displayFrame.updateGridOverlay(gridOverlay);
    }

    public Token getSelectedToken() {
        return selectedToken;
    }

    private void updateViewport() {
        // Only update viewport in Map Mode
        if (!isTokenMode) {
            Rectangle viewport = gridOverlay.getSelectedBounds();
            displayFrame.updateViewport(viewport);
        }
    }
}

/**
 * Panel for displaying the image with grid overlay
 */
class ImagePanel extends JPanel {
    private BufferedImage image;
    private GridOverlay gridOverlay;
    private TokenOverlay tokenOverlay;
    private ImageLibrary library;
    private double scale = 1.0;
    private Point offset = new Point(0, 0);
    private String draggingTokenImageId;
    private BufferedImage draggingTokenImage;
    private Point mousePosition;
    private ControllerFrame controller;

    public ImagePanel(GridOverlay gridOverlay, TokenOverlay tokenOverlay, ImageLibrary library) {
        this.gridOverlay = gridOverlay;
        this.tokenOverlay = tokenOverlay;
        this.library = library;
        setBackground(Color.DARK_GRAY);
        setPreferredSize(new Dimension(800, 600));

        MouseAdapter mouseAdapter = new MouseAdapter() {
            private Point lastPoint;

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isMiddleMouseButton(e) ||
                    (SwingUtilities.isLeftMouseButton(e) && e.isControlDown())) {
                    lastPoint = e.getPoint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastPoint != null) {
                    int dx = e.getX() - lastPoint.x;
                    int dy = e.getY() - lastPoint.y;
                    offset.translate(dx, dy);
                    lastPoint = e.getPoint();
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                lastPoint = null;
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double oldScale = scale;
                if (e.getWheelRotation() < 0) {
                    scale *= 1.1;
                } else {
                    scale /= 1.1;
                }
                scale = Math.max(0.1, Math.min(scale, 10.0));

                Point mousePoint = e.getPoint();
                offset.x = (int) (mousePoint.x - (mousePoint.x - offset.x) * scale / oldScale);
                offset.y = (int) (mousePoint.y - (mousePoint.y - offset.y) * scale / oldScale);

                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                // Track mouse position for drag preview
                if (draggingTokenImageId != null) {
                    mousePosition = e.getPoint();
                    repaint();
                }
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        addMouseWheelListener(mouseAdapter);
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        if (image != null) {
            scale = Math.min(
                getWidth() / (double) image.getWidth(),
                getHeight() / (double) image.getHeight()
            ) * 0.9;
            offset.x = (getWidth() - (int) (image.getWidth() * scale)) / 2;
            offset.y = (getHeight() - (int) (image.getHeight() * scale)) / 2;
        }
        repaint();
    }

    public void setController(ControllerFrame controller) {
        this.controller = controller;
    }

    public void setDraggingToken(String imageId, BufferedImage tokenImage) {
        this.draggingTokenImageId = imageId;
        this.draggingTokenImage = tokenImage;
    }

    public String getDraggingTokenImageId() {
        return draggingTokenImageId;
    }

    public BufferedImage getDraggingTokenImage() {
        return draggingTokenImage;
    }

    public Point getImagePoint(Point panelPoint) {
        if (image == null) return null;

        int x = (int) ((panelPoint.x - offset.x) / scale);
        int y = (int) ((panelPoint.y - offset.y) / scale);

        if (x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight()) {
            return new Point(x, y);
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (image != null) {
            int width = (int) (image.getWidth() * scale);
            int height = (int) (image.getHeight() * scale);

            g2d.drawImage(image, offset.x, offset.y, width, height, null);
            drawGrid(g2d, width, height);
            drawTokens(g2d);
            drawDraggingTokenPreview(g2d);
        }
    }

    private void drawTokens(Graphics2D g2d) {
        int squareSize = gridOverlay.getSquareSize();
        Token selectedToken = (controller != null) ? controller.getSelectedToken() : null;

        for (Token token : tokenOverlay.getAllTokens()) {
            // Load token image if not cached
            if (token.getCachedImage() == null) {
                try {
                    BufferedImage tokenImg = library.loadImage(token.getImageId());
                    token.setCachedImage(tokenImg);
                } catch (Exception e) {
                    continue; // Skip if image can't be loaded
                }
            }

            BufferedImage tokenImg = token.getCachedImage();
            if (tokenImg == null) continue;

            // Calculate token position and size in pixels
            int tokenX = token.getGridX() * squareSize;
            int tokenY = token.getGridY() * squareSize;
            int tokenWidth = token.getGridWidth() * squareSize;
            int tokenHeight = token.getGridHeight() * squareSize;

            // Apply scale and offset
            int screenX = offset.x + (int) (tokenX * scale);
            int screenY = offset.y + (int) (tokenY * scale);
            int screenWidth = (int) (tokenWidth * scale);
            int screenHeight = (int) (tokenHeight * scale);

            // Draw token image
            g2d.drawImage(tokenImg, screenX, screenY, screenWidth, screenHeight, null);

            // Draw token border (different color if selected)
            boolean isSelected = (token == selectedToken);
            if (isSelected) {
                g2d.setColor(new Color(100, 255, 100, 255));
                g2d.setStroke(new BasicStroke(4));
            } else {
                g2d.setColor(new Color(255, 100, 100, 200));
                g2d.setStroke(new BasicStroke(2));
            }
            g2d.drawRect(screenX, screenY, screenWidth, screenHeight);
        }
    }

    private void drawDraggingTokenPreview(Graphics2D g2d) {
        if (draggingTokenImage != null && mousePosition != null) {
            Point imagePoint = getImagePoint(mousePosition);
            if (imagePoint != null) {
                int squareSize = gridOverlay.getSquareSize();
                int cellX = imagePoint.x / squareSize;
                int cellY = imagePoint.y / squareSize;

                if (cellX >= 0 && cellX < gridOverlay.getGridCols() &&
                    cellY >= 0 && cellY < gridOverlay.getGridRows()) {

                    // Calculate position to draw preview
                    int tokenX = cellX * squareSize;
                    int tokenY = cellY * squareSize;
                    int tokenWidth = squareSize;
                    int tokenHeight = squareSize;

                    // Apply scale and offset
                    int screenX = offset.x + (int) (tokenX * scale);
                    int screenY = offset.y + (int) (tokenY * scale);
                    int screenWidth = (int) (tokenWidth * scale);
                    int screenHeight = (int) (tokenHeight * scale);

                    // Draw semi-transparent preview
                    Composite originalComposite = g2d.getComposite();
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
                    g2d.drawImage(draggingTokenImage, screenX, screenY, screenWidth, screenHeight, null);
                    g2d.setComposite(originalComposite);

                    // Draw preview border
                    g2d.setColor(new Color(100, 255, 100, 200));
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawRect(screenX, screenY, screenWidth, screenHeight);
                }
            }
        }
    }

    private void drawGrid(Graphics2D g2d, int displayWidth, int displayHeight) {
        int squareSize = gridOverlay.getSquareSize();
        int gridCols = gridOverlay.getGridCols();
        int gridRows = gridOverlay.getGridRows();

        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(new Color(255, 255, 0, 150));

        // Draw vertical lines
        for (int i = 0; i <= gridCols; i++) {
            int x = offset.x + (int) (i * squareSize * scale);
            g2d.drawLine(x, offset.y, x, offset.y + displayHeight);
        }

        // Draw horizontal lines
        for (int i = 0; i <= gridRows; i++) {
            int y = offset.y + (int) (i * squareSize * scale);
            g2d.drawLine(offset.x, y, offset.x + displayWidth, y);
        }

        // Highlight selected cells
        boolean[][] selected = gridOverlay.getSelectedCells();
        g2d.setColor(new Color(0, 255, 0, 100));
        for (int y = 0; y < gridRows; y++) {
            for (int x = 0; x < gridCols; x++) {
                if (selected[y][x]) {
                    Rectangle cellRect = gridOverlay.getCellRectangle(x, y);
                    if (cellRect != null) {
                        int px = offset.x + (int) (cellRect.x * scale);
                        int py = offset.y + (int) (cellRect.y * scale);
                        int pw = (int) (cellRect.width * scale);
                        int ph = (int) (cellRect.height * scale);
                        g2d.fillRect(px, py, pw, ph);
                    }
                }
            }
        }

        // Draw viewport boundary
        Rectangle viewport = gridOverlay.getSelectedBounds();
        if (viewport != null) {
            g2d.setColor(new Color(255, 0, 0, 200));
            g2d.setStroke(new BasicStroke(3));
            int vx = offset.x + (int) (viewport.x * scale);
            int vy = offset.y + (int) (viewport.y * scale);
            int vw = (int) (viewport.width * scale);
            int vh = (int) (viewport.height * scale);
            g2d.drawRect(vx, vy, vw, vh);
        }
    }
}
