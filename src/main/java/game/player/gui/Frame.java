package game.player.gui;

import javax.swing.*;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneDarkContrastIJTheme;
import game.player.Player;
import game.player.gui.interaction.Interaction;
import game.player.gui.widgets.BoardDisplay;

import java.awt.*;
import java.awt.event.WindowEvent;

public class Frame extends JFrame {

    private static Frame instance;
    private BoardDisplay boardDisplay;
    private Player player;

    public Frame(String title) {
        super(title);
        instance = this;

        FlatAtomOneDarkContrastIJTheme.setup();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(800, 800));
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        this.boardDisplay = new BoardDisplay();
        panel.add(this.boardDisplay, BorderLayout.CENTER);

        addKeyListener(new Interaction(this.boardDisplay));

        this.add(panel);
        pack();
    }

    public BoardDisplay getBoardDisplay() {
        return this.boardDisplay;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public static void close() {
        instance.dispatchEvent(new WindowEvent(instance, WindowEvent.WINDOW_CLOSING));
    }
}
