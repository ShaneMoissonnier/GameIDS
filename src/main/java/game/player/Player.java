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
    private final PlayerInfo playerInfo;

    private String areaDirectExchange;

    public Player() throws IOException, TimeoutException {
        super();
        this.playerInfo = new PlayerInfo();
        this.run();
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
        String areaFanoutExchange = areaPosition + ":fanout";

        this.subscribeToQueue(areaFanoutExchange, this::othersPositionUpdateCallback, null);
        String routingKey = this.subscribeToQueue(this.areaDirectExchange, this::areaLoginCallback, null);
        this.playerInfo.setId(routingKey);

        this.verifyPlayerRequest();
    }

    private void othersPositionUpdateCallback(String s, Delivery delivery) {
        // TODO
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
        logger.info("Connected to area, spawned at position " + playerPosition);
        // TODO : UI close  connection popup
    }

    @Override
    protected void beforeDisconnect() throws IOException {
        PlayerPresNotif notif = new PlayerPresNotif(this.playerInfo.getId(), NotificationType.LOGOUT);
        this.channel.basicPublish(this.areaDirectExchange, "area_player_presence", null, notif.toBytes());
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        Player player = new Player();
        Frame frame = new Frame("GameIDS", player);

        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }
}
