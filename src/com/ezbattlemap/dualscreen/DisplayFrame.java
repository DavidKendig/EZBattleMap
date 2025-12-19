package com.ezbattlemap.dualscreen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

/**
 * Display frame for the second screen showing the selected viewport
 */
public class DisplayFrame extends JFrame {
    private DualScreenImageApp app;
    private DisplayPanel displayPanel;
    private BufferedImage currentImage;
    private Rectangle viewport;
    private GridOverlay gridOverlay;
    private TokenOverlay tokenOverlay;

    public DisplayFrame(DualScreenImageApp app) {
        this.app = app;

        setTitle("EZBattleMap - Display");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        displayPanel = new DisplayPanel();
        add(displayPanel, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JLabel statusLabel = new JLabel("Waiting for viewport selection...");
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);
    }

    public void setImageLibrary(ImageLibrary library) {
        displayPanel.setImageLibrary(library);
    }

    public void setImage(BufferedImage image) {
        this.currentImage = image;
        displayPanel.setFullImage(image);
        updateDisplay();
    }

    public void updateGridOverlay(GridOverlay gridOverlay) {
        this.gridOverlay = gridOverlay;
        displayPanel.setGridOverlay(gridOverlay);
        updateDisplay();
    }

    public void updateTokenOverlay(TokenOverlay tokenOverlay) {
        this.tokenOverlay = tokenOverlay;
        displayPanel.setTokenOverlay(tokenOverlay);
        updateDisplay();
    }

    public void updateViewport(Rectangle viewport) {
        this.viewport = viewport;
        updateDisplay();
    }

    private void updateDisplay() {
        if (currentImage != null && viewport != null && gridOverlay != null) {
            int x = Math.max(0, viewport.x);
            int y = Math.max(0, viewport.y);
            int w = Math.min(viewport.width, currentImage.getWidth() - x);
            int h = Math.min(viewport.height, currentImage.getHeight() - y);

            if (w > 0 && h > 0) {
                BufferedImage croppedImage = currentImage.getSubimage(x, y, w, h);
                displayPanel.setViewportImage(croppedImage, viewport);
            }
        } else {
            displayPanel.setViewportImage(null, null);
        }
        displayPanel.repaint();
    }
}

/**
 * Panel for displaying the viewport image with black boxes over unselected squares
 */
class DisplayPanel extends JPanel {
    private BufferedImage fullImage;
    private BufferedImage viewportImage;
    private Rectangle viewportBounds;
    private GridOverlay gridOverlay;
    private TokenOverlay tokenOverlay;
    private ImageLibrary library;
    private double scale = 1.0;
    private Point offset = new Point(0, 0);

    public DisplayPanel() {
        setBackground(Color.BLACK);
        setupMouseControls();
    }

    public void setImageLibrary(ImageLibrary library) {
        this.library = library;
    }

    private void setupMouseControls() {
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
                if (viewportImage != null) {
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
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        addMouseWheelListener(mouseAdapter);
    }

    public void setFullImage(BufferedImage image) {
        this.fullImage = image;
        repaint();
    }

    public void setGridOverlay(GridOverlay gridOverlay) {
        this.gridOverlay = gridOverlay;
    }

    public void setTokenOverlay(TokenOverlay tokenOverlay) {
        this.tokenOverlay = tokenOverlay;
    }

    public void setViewportImage(BufferedImage image, Rectangle viewport) {
        this.viewportImage = image;
        this.viewportBounds = viewport;

        // Reset zoom and offset when new viewport is set
        if (image != null) {
            scale = Math.min(
                getWidth() / (double) image.getWidth(),
                getHeight() / (double) image.getHeight()
            ) * 0.95;
            offset.x = (getWidth() - (int) (image.getWidth() * scale)) / 2;
            offset.y = (getHeight() - (int) (image.getHeight() * scale)) / 2;
        }

        repaint();
    }

    private void drawTokens(Graphics2D g2d) {
        if (tokenOverlay == null || gridOverlay == null || library == null || viewportBounds == null) {
            return;
        }

        int squareSize = gridOverlay.getSquareSize();

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

            // Calculate token position and size in image coordinates
            int tokenX = token.getGridX() * squareSize;
            int tokenY = token.getGridY() * squareSize;
            int tokenWidth = token.getGridWidth() * squareSize;
            int tokenHeight = token.getGridHeight() * squareSize;

            // Create rectangle for token bounds
            Rectangle tokenBounds = new Rectangle(tokenX, tokenY, tokenWidth, tokenHeight);

            // Check if token intersects with viewport
            if (tokenBounds.intersects(viewportBounds)) {
                // Convert to viewport-relative coordinates
                int relX = tokenX - viewportBounds.x;
                int relY = tokenY - viewportBounds.y;

                // Apply scale and offset for display
                int screenX = offset.x + (int) (relX * scale);
                int screenY = offset.y + (int) (relY * scale);
                int screenWidth = (int) (tokenWidth * scale);
                int screenHeight = (int) (tokenHeight * scale);

                // Draw token image
                g2d.drawImage(tokenImg, screenX, screenY, screenWidth, screenHeight, null);

                // Draw token border
                g2d.setColor(new Color(255, 100, 100, 200));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRect(screenX, screenY, screenWidth, screenHeight);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (viewportImage != null && viewportBounds != null && gridOverlay != null) {
            int width = (int) (viewportImage.getWidth() * scale);
            int height = (int) (viewportImage.getHeight() * scale);

            // Draw the viewport image
            g2d.drawImage(viewportImage, offset.x, offset.y, width, height, null);

            // Draw black boxes over unselected squares
            List<Rectangle> unselectedRects = gridOverlay.getUnselectedCellRectangles();
            g2d.setColor(Color.BLACK);

            for (Rectangle cellRect : unselectedRects) {
                // Check if this cell intersects with the viewport
                if (cellRect.intersects(viewportBounds)) {
                    // Calculate the intersection
                    Rectangle intersection = cellRect.intersection(viewportBounds);

                    // Convert to viewport-relative coordinates
                    int relX = intersection.x - viewportBounds.x;
                    int relY = intersection.y - viewportBounds.y;

                    // Scale and offset for display
                    int displayX = offset.x + (int)(relX * scale);
                    int displayY = offset.y + (int)(relY * scale);
                    int displayW = (int)(intersection.width * scale);
                    int displayH = (int)(intersection.height * scale);

                    g2d.fillRect(displayX, displayY, displayW, displayH);
                }
            }

            // Draw tokens on top of everything
            drawTokens(g2d);
        } else if (fullImage != null) {
            g2d.setColor(Color.GRAY);
            g2d.setFont(new Font("Arial", Font.PLAIN, 24));
            String message = "Select grid cells on the controller to display viewport";
            FontMetrics fm = g2d.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(message)) / 2;
            int y = getHeight() / 2;
            g2d.drawString(message, x, y);
        } else {
            g2d.setColor(Color.GRAY);
            g2d.setFont(new Font("Arial", Font.PLAIN, 24));
            String message = "Waiting for image...";
            FontMetrics fm = g2d.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(message)) / 2;
            int y = getHeight() / 2;
            g2d.drawString(message, x, y);
        }
    }
}
