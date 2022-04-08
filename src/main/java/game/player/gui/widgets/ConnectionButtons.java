package game.player.gui.widgets;

import game.player.Player;
import game.player.gui.interaction.Interaction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ConnectionButtons extends JPanel implements ActionListener {
    private final Player player;
    private static JButton m_connectButton;
    private static JButton m_disconnectButton;

    private void createButtons() {
        m_connectButton = new JButton("Connect");
        m_disconnectButton = new JButton("Disconnect");

        this.add(m_connectButton, BorderLayout.LINE_START);
        this.add(m_disconnectButton, BorderLayout.LINE_END);

        m_connectButton.addActionListener(this);
        m_disconnectButton.addActionListener(this);

        m_disconnectButton.setEnabled(false);
    }

    private void setupPanel() {
        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));

        this.setPreferredSize(new Dimension(getWidth(), 60));
    }

    public ConnectionButtons(Player player) {
        this.player = player;
        this.setupPanel();
        this.createButtons();
    }

    public void setListener(Interaction interaction) {
        m_connectButton.addKeyListener(interaction);
        m_disconnectButton.addKeyListener(interaction);
    }

    public static void setLoggedIn() {
        m_connectButton.setEnabled(false);
        m_disconnectButton.setEnabled(true);
    }

    public static void setLoggedOut() {
        m_connectButton.setEnabled(true);
        m_disconnectButton.setEnabled(false);
    }

    public void login() {
        try {
            this.player.interactWithDispatcher();
            setLoggedIn();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logout() {
        try {
            this.player.disconnectFromArea(true);
            HeaderPanel.setLabelText("Not logged in");
            setLoggedOut();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object eventSource = e.getSource();

        if (eventSource == m_connectButton)
            login();
        else if (eventSource == m_disconnectButton)
            logout();
    }
}
