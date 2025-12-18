package com.ezbattlemap.dualscreen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * Main application class for dual-screen image display with controller
 */
public class DualScreenImageApp {
    private ControllerFrame controllerFrame;
    private DisplayFrame displayFrame;
    private BufferedImage currentImage;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new DualScreenImageApp();
        });
    }

    public DualScreenImageApp() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screens = ge.getScreenDevices();

        displayFrame = new DisplayFrame(this);
        controllerFrame = new ControllerFrame(this, displayFrame);

        if (screens.length > 1) {
            Rectangle screen1Bounds = screens[0].getDefaultConfiguration().getBounds();
            Rectangle screen2Bounds = screens[1].getDefaultConfiguration().getBounds();

            controllerFrame.setLocation(screen1Bounds.x, screen1Bounds.y);
            displayFrame.setLocation(screen2Bounds.x, screen2Bounds.y);
        } else {
            controllerFrame.setLocation(100, 100);
            displayFrame.setLocation(800, 100);
        }

        controllerFrame.setVisible(true);
        displayFrame.setVisible(true);

        promptForImage();
    }

    public void promptForImage() {
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
                return "Image Files (*.jpg, *.png, *.gif, *.bmp)";
            }
        });

        int result = fileChooser.showOpenDialog(controllerFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            loadImage(selectedFile);
        }
    }

    public void loadImage(File file) {
        try {
            currentImage = ImageIO.read(file);
            controllerFrame.setImage(currentImage);
            displayFrame.setImage(currentImage);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(controllerFrame,
                "Error loading image: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public BufferedImage getCurrentImage() {
        return currentImage;
    }
}
