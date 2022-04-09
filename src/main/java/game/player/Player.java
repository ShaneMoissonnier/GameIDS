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

public class Player extends ClientRabbitMQ {
    private Frame frame;

    private String id;
    private Token token;

    private boolean isLoggedIn;

    private String areaDirectExchange;
    private String areaFanoutExchange;

    private String exchangeDirectQueue;
    private String exchangeFanoutQueue;
    private String changeAreaQueue;
    private String tokenNotifyQueue;

    public Player() throws IOException, TimeoutException {
        super();
        this.isLoggedIn = false;

        this.beforeConnect();
        this.connect();
    }

    private void setFrame(Frame frame) {
        this.frame = frame;
    }

    public void interactWithDispatcher() throws IOException {
        this.id = this.subscribeToQueue(DISPATCHER_EXCHANGE, this::dispatcherCallback, null);

        QueryPosition queryPosition = new QueryPosition(this.id, SenderType.PLAYER);
        this.channel.basicPublish(DISPATCHER_EXCHANGE, "dispatcher", null, queryPosition.toBytes());
    }

    private void connectToArea(Point areaPosition) throws IOException {
        logger.info("Connecting to area at position : " + areaPosition);

        this.areaDirectExchange = areaPosition + ":direct";
        this.areaFanoutExchange = areaPosition + ":fanout";

        this.exchangeFanoutQueue = this.subscribeToQueue(areaFanoutExchange, this::othersPositionUpdateCallback, null);
        this.exchangeDirectQueue = this.subscribeToQueue(this.areaDirectExchange, this::areaLoginCallback, null);

        this.id = this.exchangeDirectQueue;

        this.changeAreaQueue = this.subscribeToQueue(this.areaDirectExchange, this::changeAreaCallback, this.id + ":change_area");
        this.tokenNotifyQueue = this.subscribeToQueue(this.areaDirectExchange, this::tokenNotifyCallback, this.id + ":token_notify");
    }

    private void connectToArea(Point areaPosition, Token token, Point position) throws IOException {
        this.connectToArea(areaPosition);

        this.isLoggedIn = true;
        PlayerFromOtherAreaNotif areaNotifMessage = new PlayerFromOtherAreaNotif(this.id, token, position);
        this.channel.basicPublish(this.areaDirectExchange, "area_from_other", null, areaNotifMessage.toBytes());
    }

    private void dispatcherCallback(String consumerTag, Delivery delivery) throws IOException {
        ResponsePosition positionResponse = ResponsePosition.fromBytes(delivery.getBody());
        Point areaPosition = positionResponse.getPosition();
        this.connectToArea(areaPosition);

        PlayerPresNotif areaNotifMessage = new PlayerPresNotif(this.id, NotificationType.LOGIN);
        channel.basicPublish(this.areaDirectExchange, "area_player_presence", null, areaNotifMessage.toBytes());
    }

    private void tokenNotifyCallback(String consumerTag, Delivery delivery) {
        TokenNotify message = TokenNotify.fromBytes(delivery.getBody());
        this.token = message.getToken();
    }

    private void othersPositionUpdateCallback(String s, Delivery delivery) {
        BoardUpdate message = BoardUpdate.fromBytes(delivery.getBody());
        this.frame.setBoardModel(message.getModel());
    }

    private void areaLoginCallback(String consumerTag, Delivery delivery) {
        ResponsePosition response = ResponsePosition.fromBytes(delivery.getBody());
        Point playerPosition = response.getPosition();
        this.isLoggedIn = true;
        logger.info("Connected to area, spawned at position " + playerPosition);
    }

    public void disconnectFromArea(boolean notifyArea) throws IOException {
        this.channel.queueUnbind(this.exchangeDirectQueue, this.areaDirectExchange, this.exchangeDirectQueue);
        this.channel.queueUnbind(this.exchangeFanoutQueue, this.areaFanoutExchange, this.exchangeFanoutQueue);
        this.channel.queueUnbind(this.changeAreaQueue, this.areaDirectExchange, this.id + ":change_area");
        this.channel.queueUnbind(this.tokenNotifyQueue, this.areaDirectExchange, this.id + ":token_notify");

        if (notifyArea) {
            PlayerPresNotif notif = new PlayerPresNotif(this.id, NotificationType.LOGOUT);
            this.channel.basicPublish(this.areaDirectExchange, "area_player_presence", null, notif.toBytes());
        }

        this.areaDirectExchange = null;
        this.areaFanoutExchange = null;
        this.exchangeDirectQueue = null;
        this.exchangeFanoutQueue = null;

        this.isLoggedIn = false;

        this.frame.setBoardModel(null);
    }

    private void changeAreaCallback(String consumerTag, Delivery delivery) throws IOException {
        ChangeAreaInstruction responsePosition = ChangeAreaInstruction.fromBytes(delivery.getBody());
        Point newArea = responsePosition.getAreaPosition();
        Point newPosition = responsePosition.getPlayerPosition();

        this.disconnectFromArea(false);
        this.connectToArea(newArea, this.token, newPosition);
    }

    public void tryToMove(Direction direction) throws IOException {
        QueryMove message = new QueryMove(this.id, direction);
        this.channel.basicPublish(
                this.areaDirectExchange,
                "area_request_move",
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
