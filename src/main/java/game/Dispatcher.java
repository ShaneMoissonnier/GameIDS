package game;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Delivery;
import game.common.ClientRabbitMQ;
import game.common.Point;
import game.common.messages.AreaPresenceNotification;
import game.common.messages.PositionResponse;
import game.common.messages.QueryPosition;
import game.common.messages.SenderType;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.TimeoutException;

public class Dispatcher extends ClientRabbitMQ {
    private final Random random;

    private final List<List<Boolean>> areas;

    public Dispatcher() throws IOException, TimeoutException {
        random = new Random();

        areas = new Vector<>();
        areas.add(new Vector<>());
        areas.get(0).add(false);

        this.run();
        this.afterDispatch();
    }

    @Override
    protected void mainBody() {
    }

    @Override
    protected void subscribeToQueues() throws IOException {
        this.subscribeToQueue(DISPATCHER_EXCHANGE, this::queryPositionCallback, "dispatcher");
        this.subscribeToQueue(DISPATCHER_EXCHANGE, this::areaLogoutCallback, "dispatcher_logout");
    }

    @Override
    protected void setupExchanges() throws IOException {
        this.declareExchange(DISPATCHER_EXCHANGE, BuiltinExchangeType.DIRECT);
    }

    private Point findHole() {
        int row_max = areas.size();
        int col_max = areas.get(0).size();

        for (int i = 0; i < row_max; i++) {
            for (int j = 0; j < col_max; j++) {
                if (!this.areas.get(i).get(j)) {
                    logger.info("Hole found : " + i + ", " + j);
                    System.out.println(areas);
                    return new Point(i, j);
                }
            }
        }


        return null;
    }

    private Point randomArea() {
        int row_max = areas.size();
        int col_max = areas.get(0).size();

        int row;
        int col;

        do {
            row = random.nextInt(row_max);
            col = random.nextInt(col_max);
        } while (!this.areas.get(row).get(col));

        return new Point(row, col);
    }

    private void resizeZones() {
        logger.info("Resizing matrix");
        for (List<Boolean> row : areas) {
            row.add(false);
        }

        int size = areas.get(0).size();
        areas.add(new Vector<>());
        for (int i = 0; i < size; i++) {
            areas.get(size - 1).add(false);
        }
    }

    private void queryPositionCallback(String consumerTag, Delivery delivery) throws IOException {
        QueryPosition query = QueryPosition.fromBytes(delivery.getBody());

        Point res;

        if (query.type == SenderType.AREA) {
            res = findHole();
            if (res == null) {
                this.resizeZones();
                res = findHole();
                if (res == null) {
                    throw new NoSuchElementException("Fatal error : could not resize the grid");
                }
            }
            this.areas.get(res.getRow()).set(res.getColumn(), true);
        } else {
            res = randomArea();
        }

        PositionResponse response = new PositionResponse(res);
        this.channel.basicPublish(DISPATCHER_EXCHANGE, query.senderId, null, response.toBytes());
    }

    private void areaLogoutCallback(String consumerTag, Delivery delivery) {
        AreaPresenceNotification msg = AreaPresenceNotification.fromBytes(delivery.getBody());
        Point coordinates = msg.getCoordinates();
        this.areas.get(coordinates.getRow()).set(coordinates.getColumn(), false);
        logger.info("Area at coordinates " + coordinates + " disconnected");
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        new Dispatcher();
    }
}
