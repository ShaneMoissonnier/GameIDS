package game.player;

import game.common.ClientRabbitMQ;
import game.player.gui.Frame;

import javax.swing.*;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Player extends ClientRabbitMQ {
    private final PlayerInfo playerInfo;

    public Player() throws IOException, TimeoutException {
        super();
        this.playerInfo = new PlayerInfo();
        this.run();
    }

    @Override
    protected void mainBody() {

    }

    @Override
    protected void subscribeToQueues() {
        // TODO: bind player to dispatcher
        //this.subscribeToQueue();
    }

    @Override
    protected void setupExchanges() {

    }

    public static void main(String[] args) throws IOException, TimeoutException {
        Player player = new Player();
        Frame frame = new Frame("GameIDS", player);
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }
}
