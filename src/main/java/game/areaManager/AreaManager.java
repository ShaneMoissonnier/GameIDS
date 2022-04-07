package game.areaManager;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Delivery;
import game.common.ClientRabbitMQ;
import game.common.Direction;
import game.common.Point;
import game.common.messages.*;
import game.player.PlayerInfo;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class AreaManager extends ClientRabbitMQ {
    public static final int N_ROWS = 6;
    public static final int N_COLUMNS = 6;

    private final Random random = new Random();

    private String DIRECT_NAME;
    private String FANOUT_NAME;

    private Point coordinates;

    private final Map<String, PlayerInfo> players;
    private final List<List<String>> map;

    private final Map<Direction, Boolean> neighborsPresent;

    public AreaManager() throws IOException, TimeoutException {
        super();

        this.players = new HashMap<>();
        this.neighborsPresent = new HashMap<>();

        this.map = new Vector<>();
        for (int i = 0; i < N_ROWS; i++) {
            map.add(new Vector<>());
            for (int j = 0; j < N_COLUMNS; j++) {
                map.get(i).add(null);
            }
        }

        this.run();
    }

    private void notifyNeighbor(Direction direction, NotificationType type, String routingKey) throws IOException {
        channel.basicPublish(
                this.getNeighborExchange(direction),
                routingKey,
                null,
                new AreaPresNotif(this.coordinates, type).toBytes()
        );
    }

    private void notifyNeighbors(NotificationType type) throws IOException {
        for (Direction direction : Direction.values()) {
            this.notifyNeighbor(direction, type, this.getPresenceNotificationKey(direction));
        }
    }

    @Override
    protected void interactWithDispatcher() throws IOException {
        String key = this.subscribeToQueue(DISPATCHER_EXCHANGE, this::dispatcherCallback, null);
        this.channel.basicPublish(DISPATCHER_EXCHANGE, "dispatcher", null, new QueryPosition(key, SenderType.AREA).toBytes());
    }

    private void dispatcherCallback(String consumerTag, Delivery delivery) throws IOException {
        ResponsePosition response = ResponsePosition.fromBytes(delivery.getBody());

        this.coordinates = response.getPosition();
        this.DIRECT_NAME = this.coordinates + ":direct";
        this.FANOUT_NAME = this.coordinates + ":fanout";
        logger.info("Received position : " + this.coordinates);
        this.afterDispatch();
    }

    private void afterDispatch() throws IOException {
        this.setupExchanges();
        this.subscribeToQueues();
        this.notifyNeighbors(NotificationType.LOGIN);
    }

    @Override
    protected void setupExchanges() throws IOException {
        this.declareExchange(this.DIRECT_NAME, BuiltinExchangeType.DIRECT);
        this.declareExchange(this.FANOUT_NAME, BuiltinExchangeType.FANOUT);

        for (Direction direction : Direction.values()) {
            this.declareExchange(this.getNeighborExchange(direction), BuiltinExchangeType.DIRECT);
            this.neighborsPresent.put(direction, false);
        }
    }

    @Override
    protected void subscribeToQueues() throws IOException {
        String areaPresenceQueue = this.channel.queueDeclare().getQueue();
        String areaPresenceResponseQueue = this.channel.queueDeclare().getQueue();
        for (Direction direction : Direction.values()) {
            this.channel.queueBind(areaPresenceQueue, this.getNeighborExchange(direction), this.getPresenceNotificationKey());
            this.channel.queueBind(areaPresenceResponseQueue, this.getNeighborExchange(direction), this.getPresenceResponseKey());
        }

        this.channel.basicConsume(areaPresenceQueue, true, this::areaPresenceNotificationCallback, consumerTag -> {
        });
        this.channel.basicConsume(areaPresenceResponseQueue, true, this::areaPresenceResponseCallback, consumerTag -> {
        });

        this.subscribeToQueue(this.DIRECT_NAME, this::playerPresenceCallback, "area_player_presence");
    }

    private void areaPresenceResponseCallback(String consumerTag, Delivery delivery) {
        AreaPresNotif presNotif = AreaPresNotif.fromBytes(delivery.getBody());
        Point otherCoords = presNotif.getPosition();
        Direction direction = this.coordinates.getDirectionOfNeighbor(otherCoords);

        switch (presNotif.getType()) {
            case LOGIN:
                this.neighborsPresent.put(direction, true);
                logger.info("New area : coordinates = " + otherCoords + ", on our " + direction.name().toLowerCase());
                break;
            case LOGOUT:
                /* Should never happen */
                break;
        }
    }

    private void areaPresenceNotificationCallback(String consumerTag, Delivery delivery) throws IOException {
        AreaPresNotif presNotif = AreaPresNotif.fromBytes(delivery.getBody());
        Point otherCoords = presNotif.getPosition();
        Direction direction = this.coordinates.getDirectionOfNeighbor(otherCoords);

        switch (presNotif.getType()) {
            case LOGIN:
                this.neighborsPresent.put(direction, true);
                logger.info("New area : coordinates = " + otherCoords + ", on our " + direction.name().toLowerCase());
                this.notifyNeighbor(direction, NotificationType.LOGIN, this.getPresenceResponseKey(direction));
                break;
            case LOGOUT:
                this.neighborsPresent.put(direction, false);
                logger.info("The area on our " + direction.name().toLowerCase() + " at coordinates (" + otherCoords + ") disconnected");
                break;
        }
    }

    private Point randomFreeTile() {
        int row;
        int col;

        do {
            row = random.nextInt(N_ROWS);
            col = random.nextInt(N_COLUMNS);
        } while (this.map.get(row).get(col) != null);

        return new Point(row, col);
    }

    private void playerPresenceCallback(String consumerTag, Delivery delivery) throws IOException {
        PlayerPresNotif notif = PlayerPresNotif.fromBytes(delivery.getBody());

        switch (notif.getType()) {
            case LOGIN:
                Point pos = this.randomFreeTile();
                this.logger.info("Player logged in : spawning them on tile " + pos);
                this.map.get(pos.getRow()).set(pos.getColumn(), notif.getSenderId());
                ResponsePosition responsePosition = new ResponsePosition(pos);
                this.channel.basicPublish(DIRECT_NAME, notif.getSenderId(), null, responsePosition.toBytes());
                break;
            case LOGOUT:
                logger.info("Player '" + notif.getSenderId() + "' logged out.");
                this.players.remove(notif.getSenderId());
                break;
        }
    }

    /**
     * Returns the exchange between two neighboring areas
     *
     * @param direction The direction towards which the other exchange is
     */
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
        this.notifyNeighbors(NotificationType.LOGOUT);

        AreaPresNotif msg = new AreaPresNotif(this.coordinates, NotificationType.LOGOUT);
        this.channel.basicPublish(DISPATCHER_EXCHANGE, "dispatcher_logout", null, msg.toBytes());
    }

    private String getPresenceNotificationKey() {
        return this.coordinates + ":presence_notify";
    }

    private String getPresenceNotificationKey(Direction direction) {
        return this.coordinates.getNeighbor(direction) + ":presence_notify";
    }

    private String getPresenceResponseKey() {
        return this.coordinates + ":presence_response";
    }

    private String getPresenceResponseKey(Direction direction) {
        return this.coordinates.getNeighbor(direction) + ":presence_response";
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        new AreaManager();
    }
}
