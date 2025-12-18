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
    private boolean dragSelectMode = false;
    private int lastCellX = -1;
    private int lastCellY = -1;
    private ImageLibraryPanel libraryPanel;

    public ControllerFrame(DualScreenImageApp app, DisplayFrame displayFrame, ImageLibrary library) {
        this.app = app;
        this.displayFrame = displayFrame;

        setTitle("EZBattleMap - Controller");
        setSize(1400, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        gridOverlay = new GridOverlay();
        imagePanel = new ImagePanel(gridOverlay);

        JPanel controlPanel = createControlPanel();

        // Create image library panel on the left
        libraryPanel = new ImageLibraryPanel(library);
        libraryPanel.setSelectionListener((imageId, image) -> {
            setImage(image);
            displayFrame.setImage(image);
        });

        add(libraryPanel, BorderLayout.WEST);
        add(new JScrollPane(imagePanel), BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        MouseAdapter gridSelectionAdapter = new MouseAdapter() {
            private boolean isDragging = false;

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && !e.isControlDown()) {
                    Point imagePoint = imagePanel.getImagePoint(e.getPoint());
                    if (imagePoint != null) {
                        int cellX = imagePoint.x / gridOverlay.getSquareSize();
                        int cellY = imagePoint.y / gridOverlay.getSquareSize();

                        if (cellX >= 0 && cellX < gridOverlay.getGridCols() &&
                            cellY >= 0 && cellY < gridOverlay.getGridRows()) {

                            // Determine if we're selecting or deselecting based on first cell
                            dragSelectMode = !gridOverlay.isCellSelected(cellX, cellY);
                            isDragging = true;
                            lastCellX = cellX;
                            lastCellY = cellY;

                            gridOverlay.selectCell(cellX, cellY, dragSelectMode);
                            imagePanel.repaint();
                            updateViewport();
                        }
                    }
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

                            // Only update if we moved to a different cell
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

            @Override
            public void mouseReleased(MouseEvent e) {
                isDragging = false;
                lastCellX = -1;
                lastCellY = -1;
            }
        };

        imagePanel.addMouseListener(gridSelectionAdapter);
        imagePanel.addMouseMotionListener(gridSelectionAdapter);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        selectImageButton = new JButton("Select Image");
        selectImageButton.addActionListener(e -> app.promptForImage());

        JLabel gridLabel = new JLabel("Square Size (pixels):");
        gridSizeSpinner = new JSpinner(new SpinnerNumberModel(100, 1, 2000, 1));
        gridSizeSpinner.addChangeListener(e -> {
            int size = (Integer) gridSizeSpinner.getValue();
            gridOverlay.setSquareSize(size);
            imagePanel.repaint();
            displayFrame.updateGridOverlay(gridOverlay);
        });

        JButton clearSelectionButton = new JButton("Clear Selection");
        clearSelectionButton.addActionListener(e -> {
            gridOverlay.clearSelection();
            imagePanel.repaint();
            displayFrame.updateViewport(null);
        });

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

    private void updateViewport() {
        Rectangle viewport = gridOverlay.getSelectedBounds();
        displayFrame.updateViewport(viewport);
    }
}

/**
 * Panel for displaying the image with grid overlay
 */
class ImagePanel extends JPanel {
    private BufferedImage image;
    private GridOverlay gridOverlay;
    private double scale = 1.0;
    private Point offset = new Point(0, 0);

    public ImagePanel(GridOverlay gridOverlay) {
        this.gridOverlay = gridOverlay;
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
