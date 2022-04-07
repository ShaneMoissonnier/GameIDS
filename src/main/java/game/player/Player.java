package game.player;

import com.rabbitmq.client.Delivery;
import game.common.ClientRabbitMQ;
import game.common.Point;
import game.common.messages.AreaPresenceNotification;
import game.common.messages.PositionResponse;
import game.player.gui.Frame;
import game.player.gui.model.BoardModel;

import javax.swing.*;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Player extends ClientRabbitMQ {
    private final PlayerInfo playerInfo;
    private Frame frame;
    private BoardModel boardModel;

    public Player(Frame frame) throws IOException, TimeoutException {
        super();
        this.playerInfo = new PlayerInfo();
        this.frame = frame;
        // TODO : a bit hacky, need refactoring
        this.boardModel = this.frame.getBoardDisplay().getModel();
        this.run();
    }

    @Override
    protected void mainBody() {

    }

    @Override
    protected void subscribeToQueues() throws IOException {
        this.subscribeToQueue(DISPATCHER_EXCHANGE, this::dispatcherCallback, null);
    }

    private void dispatcherCallback(String consumerTag, Delivery delivery) throws IOException {
        PositionResponse positionResponse = PositionResponse.fromBytes(delivery.getBody());
        Point areaPosition = positionResponse.getPosition();
        String directExchange = areaPosition+":direct";

        String routingKey = this.subscribeToQueue(directExchange, this::areaCallback, null);
        this.playerInfo.setId(routingKey);

        verifyPlayerRequest(areaPosition);
    }

    private void verifyPlayerRequest(Point areaPosition) throws IOException {
        AreaPresenceNotification areaNotificationMessage = new AreaPresenceNotification(areaPosition, null, false);
        String directExchange = areaPosition+":direct";

        channel.basicPublish(directExchange, "area", null, areaNotificationMessage.toBytes());
        // TODO : UI open connection popup
        //JOptionPane.showMessageDialog(frame, "Connexion en cours", "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void areaCallback(String consumerTag, Delivery delivery) {
        AreaPresenceNotification areaPresenceNotification = AreaPresenceNotification.fromBytes(delivery.getBody());
        Point playerPosition = areaPresenceNotification.getCoordinates();
        this.playerInfo.setPosition(playerPosition);
        // TODO : UI close  connection popup
    }

    public void askDispatcher() throws IOException {
        /*String queueName = channel.queueDeclare().getQueue();
        QueryPosition queryPosition = new QueryPosition(queueName, SenderType.PLAYER);
        channel.basicPublish(DISPATCHER_EXCHANGE, "dispatcher", null, queryPosition.toBytes());*/
    }

    @Override
    protected void setupExchanges() {

    }

    public static void main(String[] args) throws IOException, TimeoutException {
        Frame frame = new Frame("GameIDS");
        Player player = new Player(frame);
        frame.setPlayer(player);

        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }
}
