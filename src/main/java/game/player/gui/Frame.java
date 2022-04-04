package game.player.gui;

import javax.swing.*;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneDarkContrastIJTheme;
import game.player.Player;

import java.awt.*;
import java.awt.event.WindowEvent;

public class Frame extends JFrame {

    private static Frame instance;

    public Frame(String title, Player player) {
        super(title);
        instance = this;

        FlatAtomOneDarkContrastIJTheme.setup();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1256, 860));

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        this.add(panel);
        pack();
    }

    public static void close() {
        instance.dispatchEvent(new WindowEvent(instance, WindowEvent.WINDOW_CLOSING));
    }
}
