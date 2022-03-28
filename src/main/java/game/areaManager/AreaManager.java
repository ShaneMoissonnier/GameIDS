package game.areaManager;

import com.rabbitmq.client.BuiltinExchangeType;
import game.common.ClientRabbitMQ;
import game.common.Point;
import game.player.PlayerInfo;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class AreaManager extends ClientRabbitMQ {
    private final Point coordinates;

    private int idCounter;
    private final List<PlayerInfo> playersList;

    private final Map<Direction, String> neighbors;

    public AreaManager(Point coordinates) {
        super();
        this.coordinates = coordinates;
        this.idCounter = 0;
        this.playersList = new Vector<>();
        this.neighbors = new HashMap<>();
    }

    @Override
    protected void mainBody() {
    }

    @Override
    protected void setupExchanges() throws IOException {
        for (Direction direction : Direction.values()) {
            Point neighbor = this.coordinates.getNeighbor(direction);
            String name = this.coordinates + neighbor.toString();

            this.declareExchange(name, BuiltinExchangeType.DIRECT);
        }
    }

    @Override
    protected void subscribeToQueues() {

    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Error : Please provide 2 intergers");
        }

        int x = Integer.parseInt(args[0]);
        int y = Integer.parseInt(args[1]);
        Point id = new Point(x, y);

        new AreaManager(id);
    }
}
