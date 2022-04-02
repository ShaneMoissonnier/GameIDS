package game.areaManager;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Delivery;
import game.common.ClientRabbitMQ;
import game.common.Direction;
import game.common.Point;
import game.common.messages.AreaPresenceNotification;
import game.common.messages.AreaPresenceNotificationType;
import game.player.PlayerInfo;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeoutException;

public class AreaManager extends ClientRabbitMQ {
    private final Point coordinates;

    private int idCounter;
    private final List<PlayerInfo> players;

    private final Map<Direction, Boolean> neighborsPresent;

    public AreaManager(Point coordinates) throws IOException, TimeoutException {
        super();
        this.coordinates = coordinates;
        this.idCounter = 0;
        this.players = new Vector<>();
        this.neighborsPresent = new HashMap<>();
        this.run();
    }

    private void notifyNeighbor(Direction direction, AreaPresenceNotificationType type, boolean isResponse) throws IOException {
        channel.basicPublish(
                this.getNeighborExchange(direction),
                this.getNeighborArea(direction).toString(),
                null,
                new AreaPresenceNotification(this.coordinates, type, isResponse).toBytes()
        );
    }

    private void notifyNeighbors(AreaPresenceNotificationType type) throws IOException {
        for (Direction direction : Direction.values()) {
            this.notifyNeighbor(direction, type, false);
        }
    }

    @Override
    protected void mainBody() throws IOException {
        this.notifyNeighbors(AreaPresenceNotificationType.LOGIN);
    }

    @Override
    protected void setupExchanges() throws IOException {
        for (Direction direction : Direction.values()) {
            this.declareExchange(this.getNeighborExchange(direction), BuiltinExchangeType.DIRECT);
            this.neighborsPresent.put(direction, false);
        }
    }

    @Override
    protected void subscribeToQueues() throws IOException {
        String areaPresenceQueue = this.channel.queueDeclare().getQueue();
        for (Direction direction : Direction.values()) {
            this.channel.queueBind(areaPresenceQueue, this.getNeighborExchange(direction), this.coordinates.toString());
        }

        this.channel.basicConsume(areaPresenceQueue, true, this::areaPresenceNotificationCallback, consumerTag -> {
        });
    }

    private void areaPresenceNotificationCallback(String consumerTag, Delivery delivery) throws IOException {
        AreaPresenceNotification presenceNotification = AreaPresenceNotification.fromBytes(delivery.getBody());
        Point otherCoords = presenceNotification.getSenderCoordinates();
        Direction direction = this.coordinates.getDirectionOfNeighbor(otherCoords);

        switch (presenceNotification.getType()) {
            case LOGIN:
                this.neighborsPresent.put(direction, true);
                logger.info("New area : coordinates = " + otherCoords + ", on our " + direction.name().toLowerCase());

                if (!presenceNotification.isResponse()) {
                    this.notifyNeighbor(direction, AreaPresenceNotificationType.LOGIN, true);
                }

                break;
            case LOGOUT:
                this.neighborsPresent.put(direction, false);
                logger.info("The area on our " + direction.name().toLowerCase() + " at coordinates (" + otherCoords + ") disconnected");

                break;
        }

    }

    private String getNeighborExchange(Direction direction) {
        switch (direction) {
            case UP:
            case LEFT:
                return this.getNeighborArea(direction) + "," + this.coordinates;
            case DOWN:
            case RIGHT:
                return this.coordinates + "," + this.getNeighborArea(direction);
            default:
                throw new IllegalArgumentException();
        }
    }

    private Point getNeighborArea(Direction direction) {
        return this.coordinates.getNeighbor(direction);
    }

    @Override
    protected void beforeDisconnect() throws IOException {
        this.notifyNeighbors(AreaPresenceNotificationType.LOGOUT);
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        if (args.length != 2) {
            System.out.println("Error : Please provide 2 integers");
            return;
        }

        int x = Integer.parseInt(args[0]);
        int y = Integer.parseInt(args[1]);
        Point coords = new Point(x, y);

        new AreaManager(coords);
    }
}