package game.player.gui;

import javax.swing.*;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneDarkContrastIJTheme;
import game.player.Player;
import game.player.gui.interaction.Interaction;
import game.player.gui.widgets.BoardDisplay;
import game.player.gui.widgets.ConnectionButtons;
import game.player.gui.widgets.HeaderPanel;

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

        this.boardDisplay = new BoardDisplay();

        HeaderPanel header = new HeaderPanel();
        ConnectionButtons connection = new ConnectionButtons(this.player);
        JPanel panel = new JPanel();

        panel.setLayout(new BorderLayout());
        panel.add(this.boardDisplay, BorderLayout.CENTER);
        panel.add(header, BorderLayout.NORTH);
        panel.add(connection, BorderLayout.SOUTH);

        Interaction interaction = new Interaction(this.boardDisplay);
        addKeyListener(interaction);
        connection.setListener(interaction);

        this.setFocusable(true);

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
