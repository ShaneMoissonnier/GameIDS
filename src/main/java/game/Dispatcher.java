package game;

import com.rabbitmq.client.BuiltinExchangeType;
import game.common.ClientRabbitMQ;
import game.common.Point;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

public class Dispatcher extends ClientRabbitMQ {
    private final List<Point> zones;

    public Dispatcher() {
        zones = new Vector<>();
    }

    @Override
    protected void mainBody() throws IOException {

    }

    @Override
    protected void subscribeToQueues() throws IOException {

    }

    @Override
    protected void setupExchanges() throws IOException {
        this.declareExchange(DISPATCHER_EXCHANGE, BuiltinExchangeType.DIRECT);
    }

    public static void main(String[] args) {
        new Dispatcher();
    }
}
