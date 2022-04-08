package game.player;

import com.rabbitmq.client.Delivery;
import game.common.ClientRabbitMQ;
import game.common.Point;
import game.common.messages.*;
import game.player.gui.Frame;

import javax.swing.*;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Player extends ClientRabbitMQ {
    private Frame frame;

    private final PlayerInfo playerInfo;

    private boolean isLoggedIn;

    private String areaDirectExchange;
    private String areaFanoutExchange;

    private String exchangeDirectQueue;
    private String exchangeFanoutQueue;

    public Player() throws IOException, TimeoutException {
        super();
        this.playerInfo = new PlayerInfo();
        this.isLoggedIn = false;

        this.beforeConnect();
        this.connect();
    }

    private void setFrame(Frame frame) {
        this.frame = frame;
    }

    public void interactWithDispatcher() throws IOException {
        String id = this.subscribeToQueue(DISPATCHER_EXCHANGE, this::dispatcherCallback, null);
        this.playerInfo.setId(id);

        QueryPosition queryPosition = new QueryPosition(this.playerInfo.getId(), SenderType.PLAYER);
        this.channel.basicPublish(DISPATCHER_EXCHANGE, "dispatcher", null, queryPosition.toBytes());
    }

    private void dispatcherCallback(String consumerTag, Delivery delivery) throws IOException {
        ResponsePosition positionResponse = ResponsePosition.fromBytes(delivery.getBody());
        Point areaPosition = positionResponse.getPosition();
        logger.info("Connecting to area at position : " + areaPosition);

        this.areaDirectExchange = areaPosition + ":direct";
        this.areaFanoutExchange = areaPosition + ":fanout";

        this.exchangeFanoutQueue = this.subscribeToQueue(areaFanoutExchange, this::othersPositionUpdateCallback, null);
        this.exchangeDirectQueue = this.subscribeToQueue(this.areaDirectExchange, this::areaLoginCallback, null);
        this.playerInfo.setId(this.exchangeDirectQueue);

        this.verifyPlayerRequest();
    }

    private void othersPositionUpdateCallback(String s, Delivery delivery) {
        BoardUpdate message = BoardUpdate.fromBytes(delivery.getBody());
        this.frame.setBoardModel(message.getModel());
    }

    private void verifyPlayerRequest() throws IOException {
        PlayerPresNotif areaNotifMessage = new PlayerPresNotif(this.playerInfo.getId(), NotificationType.LOGIN);
        channel.basicPublish(this.areaDirectExchange, "area_player_presence", null, areaNotifMessage.toBytes());
        // TODO : UI open connection popup
        //JOptionPane.showMessageDialog(frame, "Connexion en cours", "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void areaLoginCallback(String consumerTag, Delivery delivery) {
        ResponsePosition response = ResponsePosition.fromBytes(delivery.getBody());
        Point playerPosition = response.getPosition();
        this.playerInfo.setPosition(playerPosition);
        this.isLoggedIn = true;
        logger.info("Connected to area, spawned at position " + playerPosition);
        // TODO : UI close  connection popup
    }

    public void disconnectFromArea() throws IOException {
        this.channel.queueUnbind(this.exchangeDirectQueue, this.areaDirectExchange, this.exchangeDirectQueue);
        this.channel.queueUnbind(this.exchangeFanoutQueue, this.areaFanoutExchange, this.exchangeFanoutQueue);

        PlayerPresNotif notif = new PlayerPresNotif(this.playerInfo.getId(), NotificationType.LOGOUT);
        this.channel.basicPublish(this.areaDirectExchange, "area_player_presence", null, notif.toBytes());

        this.areaDirectExchange = null;
        this.areaFanoutExchange = null;
        this.exchangeDirectQueue = null;
        this.exchangeFanoutQueue = null;

        this.isLoggedIn = false;

        this.frame.setBoardModel(null);
    }

    @Override
    protected void beforeDisconnect() throws IOException {
        if (this.isLoggedIn) {
            this.disconnectFromArea();
        }
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        Player player = new Player();
        Frame frame = new Frame("GameIDS", player);
        player.setFrame(frame);

        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }
}
