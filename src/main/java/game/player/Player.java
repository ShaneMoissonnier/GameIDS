package game.player;

import com.rabbitmq.client.Delivery;
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
