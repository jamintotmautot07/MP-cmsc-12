package util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class UtilityTool {

    public static BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        // Null-safe guard so callers do not need to repeat this check themselves.
        if (originalImage == null) {
            return null;
        }

        // TYPE_INT_ARGB preserves transparency, which is important for sprites and tiles.
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImage.createGraphics();

        // Bilinear interpolation is a simple quality improvement over nearest-neighbor scaling.
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(originalImage, 0, 0, width, height, null);
        g2.dispose();
        return resizedImage;
    }

}
