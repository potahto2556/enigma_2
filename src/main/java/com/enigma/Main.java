package com.enigma;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/** Starts the graphical game window. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String name = JOptionPane.showInputDialog(null,
                    "Detective, what is your name?", "Start ENIGMA", JOptionPane.QUESTION_MESSAGE);
            if (name == null) name = "Detective";

            GameController controller = new GameController(name);
            GameWindow window = new GameWindow(controller);
            window.setVisible(true);
        });
    }
}
