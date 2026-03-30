package main;

import java.awt.CardLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import panels.OpeningPanel;

public class BaseFrame extends JFrame{

    private CardLayout cardLayout;
    private JPanel container;

    private OpeningPanel openPanel;

    public BaseFrame() {
        setTitle("Hawak ko ang Bit: The Final Bit");
        
        setSize(780, 500);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        openPanel = new OpeningPanel();

        cardLayout = new CardLayout();
        container = new JPanel(cardLayout);

        container.add(openPanel, "Openning");

        add(container);

        cardLayout.show(container, "Openning");

        openPanel.exitButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                BaseFrame.this,
                "Are you sure you want to exit?",
                "WARNING: Close program",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if(choice == JOptionPane.YES_OPTION) {
                this.dispose();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                openPanel.exitButton.doClick();
            }
        });
    }
}
