package game.player;

import com.rabbitmq.client.Delivery;
import game.common.ClientRabbitMQ;
import game.common.Direction;
import game.common.Point;
import game.common.boardModel.Token;
import game.common.messages.*;
import game.player.gui.Frame;

import javax.swing.*;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * This class represents a player.
 */
public class Player extends ClientRabbitMQ {
    /**
     * The UI frame.
     */
    private Frame frame;

    /**
     * The player's ID.
     */
    private String id;

    /**
     * The player's token.
     */
    private Token token;

    /**
     * Whether the player is logged in an area or not.
     */
    private boolean isLoggedIn;

    /* The exchanges used to communicate with the area manager */
    private String areaDirectExchange;
    private String areaFanoutExchange;

    /* The queues the player will subscribe to */
    private String loginQueue;
    private String exchangeFanoutQueue;
    private String changeAreaQueue;
    private String tokenNotifyQueue;
    private String neighborsNotifyQueue;
    private String helloNotifyQueue;

    public Player() throws IOException, TimeoutException {
        super();
        this.isLoggedIn = false;

        this.beforeConnect();
        this.connect();
    }

    private void setFrame(Frame frame) {
        this.frame = frame;
    }

    /**
     * This method is used for the initial interaction with the dispatcher.
     * <p>
     * In this interaction, the player asks the dispatcher for an area in which to spawn by sending a
     * {@link QueryPosition} message.
     */
    public void interactWithDispatcher() throws IOException {
        this.id = this.subscribeToQueue(DISPATCHER_EXCHANGE, this::dispatcherCallback, null);

        QueryPosition queryPosition = new QueryPosition(this.id, SenderType.PLAYER);
        this.channel.basicPublish(DISPATCHER_EXCHANGE, "dispatcher_login", null, queryPosition.toBytes());
    }

    /**
     * This method connects the player to an area.
     * <p>
     * This method is only called when the areaPosition has been given to the player by the Dispatcher.
     * If we are connecting to an area because we came in from another area, see {@link #connectToArea(Point, Token, Point)}.
     *
     * @param areaPosition The position of the area we are connecting to.
     */
    private void connectToArea(Point areaPosition) throws IOException {
        logger.info("Connecting to area at position : " + areaPosition);

        this.areaDirectExchange = areaPosition + ":direct";
        this.areaFanoutExchange = areaPosition + ":fanout";

        this.exchangeFanoutQueue = this.subscribeToQueue(areaFanoutExchange, this::boardUpdateCallback, null);

        this.loginQueue = this.subscribeToQueue(this.areaDirectExchange, this::areaLoginCallback, null);
        this.id = this.loginQueue;

        this.changeAreaQueue = this.subscribeToQueue(this.areaDirectExchange, this::changeAreaCallback, this.id + ":change_area");
        this.tokenNotifyQueue = this.subscribeToQueue(this.areaDirectExchange, this::tokenNotifyCallback, this.id + ":token_notify");
        this.neighborsNotifyQueue = this.subscribeToQueue(this.areaDirectExchange, this::neighborsNotifyCallback, this.id + ":neighbors_notify");
        this.helloNotifyQueue = this.subscribeToQueue(this.areaDirectExchange, this::helloNotifyCallback, this.id + ":hello_notify");
    }

    /**
     * This method connects the player to an area.
     * <p>
     * This method is only called when the player comes in from another area.
     * If we are coming from the Dispatcher, see {@link #connectToArea(Point)}.
     *
     * @param areaPosition The position of the area we are connecting to.
     * @param token        The player's token.
     * @param position     The player's position in the area.
     */
    private void connectToArea(Point areaPosition, Token token, Point position) throws IOException {
        this.connectToArea(areaPosition);

        this.isLoggedIn = true;
        PlayerFromOtherAreaNotif areaNotifMessage = new PlayerFromOtherAreaNotif(this.id, token, position);
        this.channel.basicPublish(this.areaDirectExchange, "area_from_other", null, areaNotifMessage.toBytes());
    }

    /**
     * This callback method is called when the dispatcher reply to the request sent in the {@link #interactWithDispatcher()}
     * method.
     *
     * @param delivery Holds a serialized {@link ResponsePosition} message.
     */
    private void dispatcherCallback(String consumerTag, Delivery delivery) throws IOException {
        ResponsePosition positionResponse = ResponsePosition.fromBytes(delivery.getBody());
        Point areaPosition = positionResponse.getPosition();

        /* We connect to the area and notify its manager of our presence. */
        this.connectToArea(areaPosition);
        PlayerPresNotif areaNotifMessage = new PlayerPresNotif(this.id, NotificationType.LOGIN);
        channel.basicPublish(this.areaDirectExchange, "area_player_presence", null, areaNotifMessage.toBytes());
    }

    /**
     * This callback method is called when another player says hello to us.
     *
     * @param delivery Holds a serialized {@link QueryHello} message
     */
    private void helloNotifyCallback(String consumerTag, Delivery delivery) {
        QueryHello queryHello = (QueryHello) QueryHello.fromBytes(delivery.getBody());
        PlayerInfos playerInfos = queryHello.getPlayerInfos();
        Point playerPosition = playerInfos.getPlayerPosition();

        // TODO : draw something on playerPosition to show hello message
        logger.info("Player at coordinates " + playerPosition + " said hello!");
    }

    /**
     * This callback method is called when the area manager responds to our {@link QueryNeighbors} message.
     *
     * @param delivery Holds a serialized {@link ResponseNeighbors} message.
     */
    private void neighborsNotifyCallback(String consumerTag, Delivery delivery) throws IOException {
        ResponseNeighbors responseNeighbors = ResponseNeighbors.fromBytes(delivery.getBody());

        PlayerInfos selfInfo = new PlayerInfos(this.id, responseNeighbors.getPlayerPosition());

        for (Direction direction : Direction.values()) {
            /* We say hello to every neighbor. */
            PlayerInfos playerInfos = responseNeighbors.getNeighborInfo(direction);
            QueryHello queryHello = new QueryHello(this.id, selfInfo);

            this.channel.basicPublish(
                    this.areaDirectExchange,
                    playerInfos.getPlayerId() + ":hello_notify",
                    null,
                    queryHello.toBytes()
            );
        }
    }

    /**
     * This callback method is called when the area manager sends our token to us.
     *
     * @param delivery Holds a serialized {@link TokenNotify} message.
     */
    private void tokenNotifyCallback(String consumerTag, Delivery delivery) {
        TokenNotify message = TokenNotify.fromBytes(delivery.getBody());
        this.token = message.getToken();
    }

    /**
     * This callback method is called when the area manager notifies us of a change on the board.
     *
     * @param delivery Holds a serialized {@link BoardUpdate} message.
     */
    private void boardUpdateCallback(String s, Delivery delivery) {
        BoardUpdate message = BoardUpdate.fromBytes(delivery.getBody());
        this.frame.setBoardModel(message.getModel());
    }

    /**
     * This callback method is called when the area manager responds to our {@link QueryPosition} message.
     *
     * @param delivery Holds a serialized {@link ResponsePosition} message.
     */
    private void areaLoginCallback(String consumerTag, Delivery delivery) {
        ResponsePosition response = ResponsePosition.fromBytes(delivery.getBody());
        Point playerPosition = response.getPosition();
        this.isLoggedIn = true;
        logger.info("Connected to area, spawned at position " + playerPosition);
    }

    /**
     * Disconnects from an area by unsubscribing to every queue related to it.
     *
     * @param notifyArea Whether to notify the area manager of our leave or not.
     */
    public void disconnectFromArea(boolean notifyArea) throws IOException {
        this.channel.queueUnbind(this.loginQueue, this.areaDirectExchange, this.loginQueue);
        this.channel.queueUnbind(this.exchangeFanoutQueue, this.areaFanoutExchange, this.exchangeFanoutQueue);
        this.channel.queueUnbind(this.changeAreaQueue, this.areaDirectExchange, this.id + ":change_area");
        this.channel.queueUnbind(this.tokenNotifyQueue, this.areaDirectExchange, this.id + ":token_notify");
        this.channel.queueUnbind(this.neighborsNotifyQueue, this.areaDirectExchange, this.id + ":neighbors_notify");
        this.channel.queueUnbind(this.helloNotifyQueue, this.areaDirectExchange, this.id + ":hello_notify");

        if (notifyArea) {
            /* We only notify the area manager if we are logging off of the system.
             * If we are disconnecting from the area because we are moving to another one, our previous area manager
             * already knows, so we don't have to notify it. */
            PlayerPresNotif notif = new PlayerPresNotif(this.id, NotificationType.LOGOUT);
            this.channel.basicPublish(this.areaDirectExchange, "area_player_presence", null, notif.toBytes());
        }

        this.areaDirectExchange = null;
        this.areaFanoutExchange = null;
        this.loginQueue = null;
        this.exchangeFanoutQueue = null;

        this.isLoggedIn = false;

        this.frame.setBoardModel(null);
    }

    /**
     * This callback method is called when the area managed instructs us to switch areas.
     *
     * @param delivery Holds a serialized {@link ChangeAreaInstruction} message.
     */
    private void changeAreaCallback(String consumerTag, Delivery delivery) throws IOException {
        ChangeAreaInstruction responsePosition = ChangeAreaInstruction.fromBytes(delivery.getBody());
        Point newArea = responsePosition.getAreaPosition();
        Point newPosition = responsePosition.getPlayerPosition();

        /* We disconnect from our current area. */
        this.disconnectFromArea(false);
        /* And we connect to the new one. */
        this.connectToArea(newArea, this.token, newPosition);
    }

    /**
     * This method is called when we press a movement key on the keyboard. It will let the area manager know that we are
     * trying to move in a certain direction.
     *
     * @param direction The direction the player is trying to move to.
     */
    public void tryToMove(Direction direction) throws IOException {
        QueryMove message = new QueryMove(this.id, direction);
        this.channel.basicPublish(
                this.areaDirectExchange,
                "area_request_move",
                null,
                message.toBytes()
        );
    }

    /**
     * This method is called when we press the space key. It will indicate that we wish to say hello to our neighbors.
     */
    public void trySayHello() throws IOException {
        QueryNeighbors message = new QueryNeighbors(this.id);
        this.channel.basicPublish(
                this.areaDirectExchange,
                "area_player_neighbors",
                null,
                message.toBytes()
        );
    }

    @Override
    protected void beforeDisconnect() throws IOException {
        if (this.isLoggedIn) {
            this.disconnectFromArea(true);
        }
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        Player player = new Player();
        Frame frame = new Frame("GameIDS", player);
        player.setFrame(frame);

        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }
}
