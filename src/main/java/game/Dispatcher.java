package game;

import com.rabbitmq.client.Delivery;
import game.common.ClientRabbitMQ;
import game.common.Point;
import game.common.messages.AreaPresNotif;
import game.common.messages.QueryPosition;
import game.common.messages.ResponsePosition;
import game.common.messages.SenderType;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.TimeoutException;

public class Dispatcher extends ClientRabbitMQ {
    private final Random random;

    private final int nbSelfStartedAreas;
    private final List<Process> selfStartedAreas;

    private final List<List<Boolean>> areas;

    public Dispatcher(int nbAreas) throws IOException, TimeoutException {
        random = new Random();

        areas = new Vector<>();
        areas.add(new Vector<>());
        areas.get(0).add(false);

        nbSelfStartedAreas = nbAreas;
        selfStartedAreas = new Vector<>();

        this.run();
    }

    protected void run() throws IOException, TimeoutException {
        this.beforeConnect();
        this.connect();

        this.setupExchanges();
        this.subscribeToQueues();

        this.startAreas();
    }

    private void subscribeToQueues() throws IOException {
        this.subscribeToQueue(DISPATCHER_EXCHANGE, this::queryPositionCallback, "dispatcher");
        this.subscribeToQueue(DISPATCHER_EXCHANGE, this::areaLogoutCallback, "dispatcher_logout");
    }

    private void setupExchanges() throws IOException {
        this.declareDirectExchange(DISPATCHER_EXCHANGE);
    }

    private void startAreas() throws IOException {
        String cmd = "mvn";
        String arg = "exec:java@launch-area-manager";
        ProcessBuilder builder = new ProcessBuilder(cmd, arg);

        logger.info("Starting " + this.nbSelfStartedAreas + " areas...");
        for (int i = 1; i <= this.nbSelfStartedAreas; i++) {
            this.selfStartedAreas.add(builder.start());
            logger.info("    - Started " + i + " areas");
        }
        logger.info("Finished starting areas");
    }

    private void stopSelfStartedAreas() {
        this.logShutdown("Stopping " + this.nbSelfStartedAreas + " areas started by the dispatcher...");
        for (Process p : this.selfStartedAreas) {
            p.destroy();
        }

        this.selfStartedAreas.clear();
        this.logShutdown("Finished stopping areas");
    }

    private Point findHole() {
        int row_max = areas.size();
        int col_max = areas.get(0).size();

        for (int i = 0; i < row_max; i++) {
            for (int j = 0; j < col_max; j++) {
                if (!this.areas.get(i).get(j)) {
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

        if (query.getType() == SenderType.AREA) {
            logger.info("An area logged in");
            res = findHole();
            if (res == null) {
                this.resizeZones();
                res = findHole();
                if (res == null) {
                    throw new NoSuchElementException("Fatal error : could not resize the grid");
                }
            }
            logger.info("Found free space at position " + res);
            this.areas.get(res.getRow()).set(res.getColumn(), true);
        } else {
            res = randomArea();
            logger.info("A player logged in, redirecting to area at position " + res);
        }

        ResponsePosition response = new ResponsePosition(res);
        this.channel.basicPublish(DISPATCHER_EXCHANGE, query.getSenderId(), null, response.toBytes());
    }

    private void areaLogoutCallback(String consumerTag, Delivery delivery) {
        AreaPresNotif msg = AreaPresNotif.fromBytes(delivery.getBody());
        Point coordinates = msg.getPosition();
        this.areas.get(coordinates.getRow()).set(coordinates.getColumn(), false);
        logger.info("Area at coordinates " + coordinates + " disconnected");
    }

    @Override
    protected void beforeDisconnect() {
        this.stopSelfStartedAreas();
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        int nbAreas = 0;
        if (args.length == 1) {
            nbAreas = Integer.parseInt(args[0]);
        } else if (args.length > 0) {
            System.out.println("Too many arguments. Please only specify the number of areas to be launched by the dispatcher");
            System.exit(1);
        }

        new Dispatcher(nbAreas);
    }
}
