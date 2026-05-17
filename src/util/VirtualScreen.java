package util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;

/**
 * Creates a scaled drawing context for the game's fixed virtual resolution.
 */
public final class VirtualScreen {
    private VirtualScreen() {
    }

    /**
     * Returns a Graphics2D translated and scaled so virtual game coordinates fit inside the component.
     */
    public static Graphics2D create(Graphics g, JComponent component) {
        Graphics2D screen = (Graphics2D) g;
        int componentWidth = Math.max(1, component.getWidth());
        int componentHeight = Math.max(1, component.getHeight());

        screen.setColor(Color.BLACK);
        screen.fillRect(0, 0, componentWidth, componentHeight);

        double scaleX = (double) componentWidth / Constants.screenWidth;
        double scaleY = (double) componentHeight / Constants.screenHeight;
        double scale = Math.min(scaleX, scaleY);

        int scaledWidth = (int) Math.round(Constants.screenWidth * scale);
        int scaledHeight = (int) Math.round(Constants.screenHeight * scale);
        int offsetX = (componentWidth - scaledWidth) / 2;
        int offsetY = (componentHeight - scaledHeight) / 2;

        Graphics2D virtualGraphics = (Graphics2D) screen.create();
        virtualGraphics.translate(offsetX, offsetY);
        virtualGraphics.scale(scale, scale);
        virtualGraphics.setClip(0, 0, Constants.screenWidth, Constants.screenHeight);
        virtualGraphics.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );
        virtualGraphics.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );
        return virtualGraphics;
    }
}
