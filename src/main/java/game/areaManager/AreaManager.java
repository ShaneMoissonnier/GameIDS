package game.areaManager;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Delivery;
import game.common.ClientRabbitMQ;
import game.common.Direction;
import game.common.Point;
import game.common.boardModel.BoardModel;
import game.common.boardModel.Token;
import game.common.messages.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeoutException;


/**
 * This class represents an area manager.
 * <p>
 * The goal of this entity is to manage an area. Specifically the players that are currently in that area, and the
 * neighboring areas.
 */
public class AreaManager extends ClientRabbitMQ {
    public static final int N_ROWS = 6;
    public static final int N_COLUMNS = 6;

    private final Random random = new Random();

    /**
     * The name of the direct exchange of the area.
     */
    private String DIRECT_NAME;

    /**
     * The name of the fanout of the area.
     */
    private String FANOUT_NAME;

    /**
     * The coordinates of the area in the area grid.
     */
    private Point coordinates;

    /**
     * A map of the players currently in this area.
     * <p>
     * This maps player IDs to positions in the board.
     */
    private final PlayerMap players;

    /**
     * The board representing the area. This board holds a {@link Token} for each player, but it does not know which
     * token corresponds to which player.
     */
    private final BoardModel boardModel;

    /**
     * For each direction, this indicates whether an area manager currently manages the neighboring area in this direction.
     */
    private final Map<Direction, Boolean> neighborsPresent;

    public AreaManager() throws IOException, TimeoutException {
        super();

        this.players = new PlayerMap(this.channel, N_ROWS, N_COLUMNS);
        this.neighborsPresent = new HashMap<>();

        boardModel = new BoardModel();

        this.beforeConnect();
        this.connect();
        this.interactWithDispatcher();
    }

    /**
     * This method notifies players of changes in the board by sending them a {@link BoardUpdate} message.
     */
    private void notifyPlayers() throws IOException {
        BoardUpdate message = new BoardUpdate(this.boardModel);
        this.channel.basicPublish(this.FANOUT_NAME, "", null, message.toBytes());
    }

    /**
     * This method notifies a neighboring area when the area manager logs in or out of the system.
     *
     * @param direction  The direction of the neighbor we are notifying.
     * @param type       Either LOGIN or LOGOUT
     * @param routingKey The routing key routing to the neighbor.
     */
    private void notifyNeighbor(Direction direction, NotificationType type, String routingKey) throws IOException {
        channel.basicPublish(
                this.getNeighborExchange(direction),
                routingKey,
                null,
                new AreaPresNotif(this.coordinates, type).toBytes()
        );
    }

    /**
     * Notify every neighboring area when the area manager logs in or out of the system.
     *
     * @param type Either LOGIN or LOGOUT.
     */
    private void notifyNeighbors(NotificationType type) throws IOException {
        for (Direction direction : Direction.values()) {
            this.notifyNeighbor(direction, type, this.getPresenceNotificationKey(direction));
        }
    }

    /**
     * This method is used for the initial interaction with the dispatcher.
     * <p>
     * In this interaction, the area manager asks the dispatcher for a position in the area grid by sending a
     * {@link QueryPosition} message.
     */
    private void interactWithDispatcher() throws IOException {
        String key = this.subscribeToQueue(DISPATCHER_EXCHANGE, this::dispatcherCallback, null);
        this.channel.basicPublish(DISPATCHER_EXCHANGE, "dispatcher", null, new QueryPosition(key, SenderType.AREA).toBytes());
    }

    /**
     * This callback method is called when the dispatcher reply to the request sent in the {@link #interactWithDispatcher()}
     * method.
     *
     * @param delivery Holds a serializes {@link ResponsePosition} message.
     */
    private void dispatcherCallback(String consumerTag, Delivery delivery) throws IOException {
        ResponsePosition response = ResponsePosition.fromBytes(delivery.getBody());

        this.coordinates = response.getPosition();
        this.DIRECT_NAME = this.coordinates + ":direct";
        this.FANOUT_NAME = this.coordinates + ":fanout";
        this.players.setAreaPosition(coordinates);

        logger.info("Received position : " + this.coordinates);
        /* We finished interacting with the dispatcher */
        this.afterDispatch();
    }

    /**
     * This method is called once the interaction with the dispatcher is done.
     * <p>
     * By this point, the area manager knows the position of the area it is managing, so this method is used to set up
     * the necessary queues and exchanges.
     */
    private void afterDispatch() throws IOException {
        this.boardModel.setAreaPosition(this.coordinates);

        this.setupExchanges();
        this.subscribeToQueues();
        /* We just logged in : we notify the neighbors */
        this.notifyNeighbors(NotificationType.LOGIN);
    }

    private void setupExchanges() throws IOException {
        logger.info("Declaring exchanges...");

        /* We declare the fanout and direct exchanges of the area */
        this.declareDirectExchange(this.DIRECT_NAME);
        /* The fanout exchange does not autodelete because the area manager will not subscribe to it, meaning that it
         * would be deleted the moment the only player in the area leaves. */
        this.declareExchange(this.FANOUT_NAME, BuiltinExchangeType.FANOUT, false);

        /* We have one exchange in every direction, to connect the area manager with the neighboring managers. */
        for (Direction direction : Direction.values()) {
            this.declareDirectExchange(this.getNeighborExchange(direction));
            /* We don't know yet if the neighbor area has a manager, so we assume it does not for now. */
            this.neighborsPresent.put(direction, false);
        }
        logger.info("Exchange declaration done");
    }

    private void subscribeToQueues() throws IOException {
        /* These four queues are used to communicate with the neighboring area managers, meaning that they have to be
         * bound to the four directional exchanges. */
        String areaPresenceQueue = this.channel.queueDeclare().getQueue();
        String areaPresenceResponseQueue = this.channel.queueDeclare().getQueue();
        String tileAvailableQueue = this.channel.queueDeclare().getQueue();
        String tileAvailableResponseQueue = this.channel.queueDeclare().getQueue();

        for (Direction direction : Direction.values()) {
            this.channel.queueBind(areaPresenceQueue, this.getNeighborExchange(direction), this.getPresenceNotificationKey());
            this.channel.queueBind(areaPresenceResponseQueue, this.getNeighborExchange(direction), this.getPresenceResponseKey());
            this.channel.queueBind(tileAvailableQueue, this.getNeighborExchange(direction), this.getQueryTileAvailableKey());
            this.channel.queueBind(tileAvailableResponseQueue, this.getNeighborExchange(direction), this.getResponseTileAvailableKey());
        }

        this.channel.basicConsume(areaPresenceQueue, true, this::areaPresenceNotificationCallback, consumerTag -> {
        });
        this.channel.basicConsume(areaPresenceResponseQueue, true, this::areaPresenceResponseCallback, consumerTag -> {
        });
        this.channel.basicConsume(tileAvailableQueue, true, this::tileAvailableCallback, consumerTag -> {
        });
        this.channel.basicConsume(tileAvailableResponseQueue, true, this::tileAvailableResponseCallback, consumerTag -> {
        });

        /* Those three queues are used to communicate with the players, and are therefore only bound to the area's
         * direct exchange. */
        this.subscribeToQueue(this.DIRECT_NAME, this::playerPresenceCallback, "area_player_presence");
        this.subscribeToQueue(this.DIRECT_NAME, this::playerMoveRequestCallback, "area_request_move");
        this.subscribeToQueue(this.DIRECT_NAME, this::playerFromOtherAreaCallback, "area_from_other");
    }

    /**
     * When given a point just outside this area's boundary, this method returns the point relative to the area it's
     * in.
     * <p>
     * Let's say we are the area A and the area B is on our left. The point (4, -1) is on the left of the area A, so it
     * is on the rightmost column of the area B, so this method will return (4, m - 1), where m is the number of column
     * in the area B. In this case, the row does not change.
     *
     * @param position  The point we are converting
     * @param direction The direction we are going to reach the point. i.e. the direction of the other area relative
     *                  to this one.
     * @return The point relative to the other area.
     */
    private Point convertToRemotePosition(Point position, Direction direction) {
        switch (direction) {
            case UP:
                return new Point(N_ROWS - 1, position.getColumn());
            case DOWN:
                return new Point(0, position.getColumn());
            case LEFT:
                return new Point(position.getRow(), N_COLUMNS - 1);
            case RIGHT:
                return new Point(position.getRow(), 0);
            default:
                return null;
        }
    }

    /**
     * This method sends a {@link QueryTileAvailable} to a neighboring area to know if a certain tile is available.
     * <p>
     * This method is only called when the player is trying to move to another area.
     *
     * @param playerMove The move the player is trying to make. Its direction is used to know which area we must send
     *                   the query to.
     */
    private void sendMoveToOutboundArea(QueryMove playerMove) throws IOException {
        Point start = this.players.get(playerMove.getSenderId());
        Direction direction = playerMove.getDirection();

        /* The exchange connecting this area to the area the player is trying to move to. */
        String outBoundAreaExchange = getNeighborExchange(direction);

        /* This is the tile the player will end up on if they are allowed to move. */
        Point remoteDest = this.convertToRemotePosition(start, direction);

        /* The direction we are related to the other area. This is used by the other area to respond to our query */
        Direction convertedDirection = direction.getOpposite();

        channel.basicPublish(
                outBoundAreaExchange,
                this.getQueryTileAvailableKey(direction),
                null,
                new QueryTileAvailable(playerMove.getSenderId(), remoteDest, convertedDirection).toBytes()
        );
        logger.info("Queried the area at coordinates " + getNeighborArea(direction) + " about the availability of the tile " + remoteDest);
    }

    /**
     * This callback method is called when we receive a {@link QueryTileAvailable} query.
     *
     * @param delivery Holds a serialized {@link QueryTileAvailable} message.
     */
    private void tileAvailableCallback(String consumerTag, Delivery delivery) throws IOException {
        QueryTileAvailable query = QueryTileAvailable.fromBytes(delivery.getBody());

        String senderId = query.getSenderId();
        Point position = query.getPosition();
        Direction senderDirection = query.getSenderDirection();

        ResponseTileAvailable responseMove;
        boolean status = this.boardModel.isTileAvailable(position);
        responseMove = new ResponseTileAvailable(senderId, this.coordinates, position, status);

        this.channel.basicPublish(
                this.getNeighborExchange(senderDirection),
                this.getResponseTileAvailableKey(senderDirection),
                null,
                responseMove.toBytes()
        );
    }

    /**
     * This callback method is called when a neighboring area responds to our {@link QueryTileAvailable} message.
     *
     * @param delivery A serialized {@link ResponseTileAvailable} message.
     */
    private void tileAvailableResponseCallback(String consumerTag, Delivery delivery) throws IOException {
        ResponseTileAvailable responseMove = ResponseTileAvailable.fromBytes(delivery.getBody());

        if (responseMove.getStatus()) {
            /* The tile is available. We want to transmit the information to the player so that they can change the
             * area they subscribe to. */

            String playerId = responseMove.getId();
            Point playerPos = this.players.get(playerId);

            Point otherArea = responseMove.getAreaPosition();
            Point playerRemoteDest = responseMove.getTile();

            ChangeAreaInstruction responsePosition = new ChangeAreaInstruction(otherArea, playerRemoteDest);
            this.channel.basicPublish(
                    DIRECT_NAME,
                    playerId + ":change_area",
                    null,
                    responsePosition.toBytes()
            );

            /* The player is no longer considered to be in our area. */
            this.players.remove(playerId);
            this.boardModel.removeTokenAt(playerPos);

            /* Because a player left the area, a change occurred on the board, so we notify the other players. */
            this.notifyPlayers();
        }
        /* If the tile is not available, we never instruct the player to change area. This does not cause any issue
         * because the player is not really waiting for a response. It will only switch areas when and if instructed to. */
    }

    /**
     * When a player tries to move, it sends a {@link QueryMove} message. This callback method is called when we receive
     * such messages.
     * <p>
     * If the move is not possible (because there already is a player in the destination tile for example), the query
     * is simply ignored, and nothing happens.
     *
     * @param delivery A serialized {@link QueryMove} message
     */
    private void playerMoveRequestCallback(String consumerTag, Delivery delivery) throws IOException {
        QueryMove queryMove = QueryMove.fromBytes(delivery.getBody());

        String senderId = queryMove.getSenderId();
        Direction direction = queryMove.getDirection();

        Point start = this.players.get(senderId);
        Point dest = start.getNeighbor(direction);

        if (this.boardModel.isOutbounds(dest)) {
            /* The player is trying to leave the area. */
            if (this.neighborsPresent.get(direction)) {
                /* We ask the neighboring area if it can receive the player on the tile they are trying to move to. */
                this.sendMoveToOutboundArea(queryMove);
            }
        } else {
            /* The player is moving withing the area. */
            if (this.boardModel.isTileAvailable(dest)) {
                /* They can only move if there is no one else on the tile they are trying to move to.*/
                this.boardModel.moveToken(start, dest);
                this.players.put(senderId, dest);

                /* A change occurred on the board : we notify the other players */
                this.notifyPlayers();
            }
        }
    }

    /**
     * This callback method is called an area responds to a {@link AreaPresNotif} message.
     *
     * @param delivery A serialized {@link AreaPresNotif} message.
     */
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
                /* The neighboring area is responding to our login notification to tell us "I am here too", so they
                 * should never send a logout message. */
                break;
        }
    }

    /**
     * This callback method is called when we receive a {@link AreaPresNotif} message.
     * <p>
     * When an area manager logs into the system and is assigned an area, it sends a {@link AreaPresNotif} message to
     * its neighbors to notify them of its presence.
     *
     * @param delivery A serialized {@link AreaPresNotif} message.
     */
    private void areaPresenceNotificationCallback(String consumerTag, Delivery delivery) throws IOException {
        AreaPresNotif presNotif = AreaPresNotif.fromBytes(delivery.getBody());
        Point otherCoords = presNotif.getPosition();
        Direction direction = this.coordinates.getDirectionOfNeighbor(otherCoords);

        switch (presNotif.getType()) {
            case LOGIN:
                this.neighborsPresent.put(direction, true);
                logger.info("New area : coordinates = " + otherCoords + ", on our " + direction.name().toLowerCase());
                /* A new neighbor appeared, we notify them so that they can know we are here */
                this.notifyNeighbor(direction, NotificationType.LOGIN, this.getPresenceResponseKey(direction));
                break;
            case LOGOUT:
                this.neighborsPresent.put(direction, false);
                logger.info("The area on our " + direction.name().toLowerCase() + " at coordinates (" + otherCoords + ") disconnected");
                break;
        }
    }

    /**
     * Returns a random free tile in the area.
     *
     * @return A random free tile in the area.
     */
    private Point randomFreeTile() {
        int row;
        int col;
        Point tile;

        do {
            /* This is guaranteed to be a finite loop because this method is called only when we know there exists at
             * least one free tile */

            row = random.nextInt(N_ROWS);
            col = random.nextInt(N_COLUMNS);
            tile = new Point(row, col);
        } while (this.boardModel.hasToken(tile));

        return tile;
    }

    /**
     * This callback method is called when a player sends us a {@link PlayerPresNotif} message.
     * <p>
     * Such messages are sent when a player comes in from the dispatcher or when they live the area.
     *
     * @param delivery Holds a serialized {@link PlayerPresNotif}
     * @see #playerFromOtherAreaCallback(String, Delivery) For when the player is joining from another area
     */
    private void playerPresenceCallback(String consumerTag, Delivery delivery) throws IOException {
        PlayerPresNotif notif = PlayerPresNotif.fromBytes(delivery.getBody());

        switch (notif.getType()) {
            case LOGIN:
                Point pos = this.randomFreeTile();
                this.logger.info("Player logged in : spawning them on tile " + pos);

                /* We generate a new token to display on the board. This token is linked to the player, and will follow
                 * them when they change areas. */
                Token token = new Token();

                /* We send the token over to the player */
                TokenNotify tokenNotify = new TokenNotify(token);
                this.channel.basicPublish(DIRECT_NAME, notif.getSenderId() + ":token_notify", null, tokenNotify.toBytes());

                /* We register the player as being in the area */
                this.players.put(notif.getSenderId(), pos);
                this.boardModel.putTokenOn(token, pos);

                /* We send the spawning position to the player */
                ResponsePosition responsePosition = new ResponsePosition(pos);
                this.channel.basicPublish(DIRECT_NAME, notif.getSenderId(), null, responsePosition.toBytes());

                /* A new player came in : we notify the other players */
                this.notifyPlayers();
                break;
            case LOGOUT:
                logger.info("Player '" + notif.getSenderId() + "' logged out.");

                /* We remove the player from our list and from the board. */
                Point position = this.players.get(notif.getSenderId());
                this.players.remove(notif.getSenderId());
                this.boardModel.removeTokenAt(position);

                /* A player left : we notify the other players. */
                this.notifyPlayers();
                break;
        }
    }

    /**
     * This callback method is called when we receive a {@link PlayerFromOtherAreaNotif} message.
     * <p>
     * Such messages are sent when a player comes in from another area.
     *
     * @param delivery Holds a serialized {@link PlayerFromOtherAreaNotif} message.
     * @see #playerPresenceCallback(String, Delivery) for when they come in from the dispatcher.
     */
    private void playerFromOtherAreaCallback(String consumerTag, Delivery delivery) throws IOException {
        PlayerFromOtherAreaNotif notif = PlayerFromOtherAreaNotif.fromBytes(delivery.getBody());

        String senderId = notif.getSenderId();
        Token token = notif.getToken();
        Point pos = notif.getPosition();

        /* We register the player as being in the area */
        this.boardModel.putTokenOn(token, pos);
        this.players.put(senderId, pos);

        /* A new player came in : we notify the other players */
        this.notifyPlayers();
    }

    /**
     * Returns the exchange between two neighboring areas.
     *
     * @param direction The direction towards which the other area is.
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

    /**
     * Returns the neighboring area's position in a given direction.
     *
     * @param direction The direction towards which the other area is.
     * @return The position of the neighboring area.
     */
    private Point getNeighborArea(Direction direction) {
        return this.coordinates.getNeighbor(direction);
    }

    @Override
    protected void beforeDisconnect() throws IOException {
        this.notifyNeighbors(NotificationType.LOGOUT);

        /* We notify the dispatcher we are leaving. This allows the dispatcher to reuse the area later, when a new
         * area manager joins the system. */
        AreaPresNotif msg = new AreaPresNotif(this.coordinates, NotificationType.LOGOUT);
        this.channel.basicPublish(DISPATCHER_EXCHANGE, "dispatcher_logout", null, msg.toBytes());

        /* We delete the exchange fanout. We have to do this manually because this exchange is the only non-autodelete
         * one. */
        this.channel.exchangeDelete(FANOUT_NAME);
    }

    private String getQueryTileAvailableKey() {
        return this.coordinates + ":tile_available";
    }

    private String getQueryTileAvailableKey(Direction direction) {
        return this.coordinates.getNeighbor(direction) + ":tile_available";
    }

    private String getResponseTileAvailableKey() {
        return this.coordinates + ":tile_available_response";
    }

    private String getResponseTileAvailableKey(Direction direction) {
        return this.coordinates.getNeighbor(direction) + ":tile_available_response";
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
