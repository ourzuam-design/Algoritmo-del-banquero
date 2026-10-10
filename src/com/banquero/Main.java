package com.banquero;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;

public class Main {
    public static void main(String[] args) {
        boolean forzarCLI = false;
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--cli") || arg.equalsIgnoreCase("-c")) {
                forzarCLI = true;
                break;
            }
        }

        if (forzarCLI || GraphicsEnvironment.isHeadless()) {
            BankerCLI.ejecutarCLI();
        } else {
            try {
                javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            SwingUtilities.invokeLater(() -> {
                BankerGUI gui = new BankerGUI();
                gui.setVisible(true);
            });
        }
    }
}
