package util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JOptionPane;

import main.BaseFrame;

public class MethodUtilities {
    public static class exitAction implements ActionListener, WindowListener {
        private static BaseFrame frame;

        public exitAction(BaseFrame frame) {
            exitAction.frame = frame;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            exit();
        }

        @Override
        public void windowClosing(WindowEvent e) {
            windowExit();
        }

        @Override
        public void windowOpened(WindowEvent e) {}

        @Override
        public void windowDeiconified(WindowEvent e) {}
        @Override
        public void windowActivated(WindowEvent e) {}
        @Override
        public void windowClosed(WindowEvent e) {}
        @Override
        public void windowDeactivated(WindowEvent e) {}
        @Override
        public void windowIconified(WindowEvent e) {}

        public static void exit() {
            int choice = JOptionPane.showConfirmDialog(
                frame,
                "Are you sure you want to exit?",
                "WARNING: Close program",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if(choice == JOptionPane.YES_OPTION) {
                frame.dispose();
            } else {
                frame.gamePanel.requestFocusInWindow();
            }
        }

        public void windowExit() {
            int choice = JOptionPane.showConfirmDialog(
                frame,
                "Are you sure you want to exit?",
                "WARNING: Close program",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if(choice == JOptionPane.YES_OPTION) {
                frame.gamePanel.stopGameThread();
                frame.getCredits().stopTimer();
                frame.dispose();
            } 
        } 
    }
}
