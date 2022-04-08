package game.player.gui;

import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneDarkContrastIJTheme;
import game.common.boardModel.BoardModel;
import game.player.Player;
import game.player.gui.interaction.Interaction;
import game.player.gui.widgets.BoardDisplay;
import game.player.gui.widgets.ConnectionButtons;
import game.player.gui.widgets.HeaderPanel;

import javax.swing.*;
import java.awt.*;

public class Frame extends JFrame {
    private final BoardDisplay boardDisplay;
    private Player player;

    public Frame(String title, Player player) {
        super(title);
        this.setPlayer(player);

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

        Interaction interaction = new Interaction(this.boardDisplay, this.player);
        addKeyListener(interaction);
        connection.setListener(interaction);

        this.setFocusable(true);

        this.add(panel);
        pack();
    }

    public void setBoardModel(BoardModel model) {
        this.getBoardDisplay().setBoardModel(model);
    }

    public BoardDisplay getBoardDisplay() {
        return this.boardDisplay;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
