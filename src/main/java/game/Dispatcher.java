package game;

import com.rabbitmq.client.Delivery;
import game.common.ClientRabbitMQ;
import game.common.Point;
import game.common.messages.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * The dispatcher is a special entity that is used to coordinate the connections of other entities.
 * <p>
 * Basically, it "dispatches" new areas to a position in the area grid, and new players to a random area.
 * <p>
 * For convenience, the dispatcher can also start a number of area managers on its own.
 */
public class Dispatcher extends ClientRabbitMQ {
    private final Random random;

    /**
     * The number of area managers started by the dispatcher
     */
    private final int nbSelfStartedAreas;

    /**
     * The processes started by the dispatcher
     */
    private final List<Process> selfStartedAreas;

    /**
     * A grid representing the area grid.
     * <p>
     * Basically, since every area is the same size, we can see the disposition of the areas as if they were in a grid.
     * This way, each area is assigned a position (the top left is 0;0, the one right of it is 0;1 and so on).
     * <p>
     * The boolean in the grid at indexes i,j indicates whether there is an area in the position i,j of the area grid.
     */
    private final List<List<Boolean>> areas;

    /**
     * A list of the areas with free space.
     * <p>
     * This is used to dispatch the players to random areas when they log in.
     */
    private final List<Point> areasWithFreeSpace;

    public Dispatcher(int nbAreas) throws IOException, TimeoutException {
        random = new Random();

        areas = new Vector<>();

        /* We start with an empty area grid of size 1-1 */
        areas.add(new Vector<>());
        areas.get(0).add(false);

        nbSelfStartedAreas = nbAreas;
        selfStartedAreas = new Vector<>();
        areasWithFreeSpace = new Vector<>();

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
        this.subscribeToQueue(DISPATCHER_EXCHANGE, this::queryPositionCallback, "dispatcher_login");
        this.subscribeToQueue(DISPATCHER_EXCHANGE, this::areaLogoutCallback, "dispatcher_logout");
        this.subscribeToQueue(DISPATCHER_EXCHANGE, this::notifyFreeSpaceCallback, "dispatcher_notify_free_space");
    }

    private void setupExchanges() throws IOException {
        logger.info("Declaring exchanges...");
        this.declareDirectExchange(DISPATCHER_EXCHANGE);
        logger.info("Exchange declaration done");
    }

    /**
     * This method starts a number of area managers by spawning a new process for each area.
     */
    private void startAreas() throws IOException {
        String cmd = "mvn";
        String arg = "exec:java@launch-area-manager";
        ProcessBuilder builder = new ProcessBuilder(cmd, arg);

        logger.info("Starting " + this.nbSelfStartedAreas + " areas...");
        for (int i = 1; i <= this.nbSelfStartedAreas; i++) {
            this.selfStartedAreas.add(builder.start());
            logger.info("  - Started " + i + " areas");
        }
        logger.info("Finished starting areas");
    }

    /**
     * Sends a signal SIGTERM to every area manager that have been spawned by the dispatcher
     */
    private void stopSelfStartedAreas() {
        this.logShutdown("Stopping " + this.nbSelfStartedAreas + " areas started by the dispatcher...");
        for (Process p : this.selfStartedAreas) {
            p.destroy();
        }

        this.selfStartedAreas.clear();
        this.logShutdown("Finished stopping areas");
    }

    /**
     * Returns the position of a "hole" in the area grid, i.e. a position in which there is currently no area manager.
     *
     * @return the position of a hole in the area grid. Null if the area grid is full.
     */
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

    /**
     * Returns the position of a random area for which an area manager is currently up. The returned area if guaranteed
     * to have enough space to receive a new player.
     *
     * @return The position of a random area.
     */
    private Point randomArea() {
        int index = random.nextInt(this.areasWithFreeSpace.size());
        return areasWithFreeSpace.get(index);
    }

    /**
     * Resizes the area grid by adding an extra column on the right and an extra row on the bottom.
     */
    private void resizeAreaGrid() {
        logger.info("Resizing the area grid");
        for (List<Boolean> row : areas) {
            row.add(false);
        }

        int size = areas.get(0).size();
        areas.add(new Vector<>());
        for (int i = 0; i < size; i++) {
            areas.get(size - 1).add(false);
        }
    }

    /**
     * This callback method is called when a player or an area is trying to log in.
     *
     * @param delivery Holds a serialized {@link QueryPosition} message.
     */
    public void queryPositionCallback(String consumerTag, Delivery delivery) throws IOException {
        QueryPosition query = QueryPosition.fromBytes(delivery.getBody());

        Point res;

        if (query.getType() == SenderType.AREA) {
            /* We try to find a hole in the grid. The idea is to first fill the holes, and then make the area grid
             * bigger if necessary. */
            res = findHole();
            if (res == null) {
                /* We did not find a hole, meaning that the area grid is currently full. We have to make it bigger so
                 * that we can give a position to the area manager. */
                this.resizeAreaGrid();
                res = findHole();
                if (res == null) {
                    throw new NoSuchElementException("Fatal error : could not resize the grid");
                }
            }
            logger.info("A new area manager logged in. Assigned them to position " + res);
            this.areas.get(res.getRow()).set(res.getColumn(), true);
        } else {
            /* The requester is a player, we dispatch them to a random area */
            res = randomArea();
            logger.info("A player logged in, redirecting to area at position " + res);
        }

        /* A newly created area always have free space. */
        this.areasWithFreeSpace.add(res);

        /* We send the position back to the requester */
        ResponsePosition response = new ResponsePosition(res);
        this.channel.basicPublish(DISPATCHER_EXCHANGE, query.getSenderId(), null, response.toBytes());
    }

    /**
     * This callback method is called when an area logs out.
     *
     * @param delivery Holds a serialized {@link AreaPresNotif}
     */
    private void areaLogoutCallback(String consumerTag, Delivery delivery) {
        AreaPresNotif msg = AreaPresNotif.fromBytes(delivery.getBody());

        Point coordinates = msg.getPosition();

        /* We remove the area from the grid */
        this.areas.get(coordinates.getRow()).set(coordinates.getColumn(), false);
        this.areasWithFreeSpace.remove(coordinates);

        logger.info("Area at coordinates " + coordinates + " disconnected");
    }

    /**
     * This callback method is called when an area gets full or when some space clears up.
     *
     * @param delivery Holds a serialized {@link NotifyFreeSpace} message.
     */
    private void notifyFreeSpaceCallback(String consumerTag, Delivery delivery) {
        NotifyFreeSpace msg = NotifyFreeSpace.fromBytes(delivery.getBody());

        Point areaPosition = msg.getAreaPosition();
        if (msg.isFull()) {
            this.areasWithFreeSpace.remove(areaPosition);
        } else {
            this.areasWithFreeSpace.add(areaPosition);
        }
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
