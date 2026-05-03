package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JProgressBar;

public class PreloadProgressBar extends JProgressBar{
    public PreloadProgressBar(int min, int max) {
        super(min, max);
        this.setStringPainted(true);
        this.setForeground(new Color(255, 153, 51));
        this.setBackground(new Color(40, 40, 50));
        this.setFont(new Font("Arial", Font.BOLD, 12));
        this.setBorderPainted(false);
        this.setOpaque(true);
        this.setPreferredSize(new Dimension(400, 25));
    }
}
