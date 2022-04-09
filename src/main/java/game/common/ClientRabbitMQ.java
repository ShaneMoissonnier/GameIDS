package game.common;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

/**
 * This class represents the core of any entity that connects to the RabbitMQ Server
 */
public abstract class ClientRabbitMQ {
    /**
     * The RabbitMQ Server's host
     */
    protected static final String HOST = "localhost";

    /**
     * The name of the exchange used to communicate with the {@link game.Dispatcher}. This is the only exchange that
     * will always exist no matter the number of area managers and players currently connected.
     */
    protected static final String DISPATCHER_EXCHANGE = "dispatcher";

    protected Logger logger = null;

    protected Connection connection = null;
    protected Channel channel = null;

    public ClientRabbitMQ() {
    }

    /**
     * Configures the logger to make it reliable to use during the JVM shutdown sequence, and to change the format of
     * the messages.
     */
    private void setupLogger() {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s%n");
        System.setProperty("java.util.logging.manager", MyLogManager.class.getName());
        logger = Logger.getLogger(this.getClass().getName());
    }

    /**
     * This method is used to log during the shutdown hook.
     * <p>
     * It is necessary to bypass the logger during shutdown sequence because I did not manage to change the LogManager
     * class when running with Maven : the system property is always set too late. Because of this, if we want to log
     * something during shutdown when running with Maven, we have to use System.out instead.
     *
     * @param msg The message to log
     */
    protected void logShutdown(String msg) {
        if (System.getProperty("fromTerminal") == null) {
            /* We are executing from the IDE */
            logger.info(msg);
        } else {
            /* We are executing from the terminal */
            System.out.println("INFOS : " + msg);
        }
    }

    /**
     * This method is called right before connecting to the RabbitMQ Server.
     */
    protected void beforeConnect() {
        this.setupLogger();
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    /**
     * This method connects the client to the RabbitMQ Server.
     */
    protected void connect() throws IOException, TimeoutException {
        logger.info("Connecting to RabbitMQ-Server...");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);

        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
        logger.info("Connection successful");
    }

    /**
     * This method is called right before disconnecting from the RabbitMQ Server.
     */
    protected void beforeDisconnect() throws IOException {
    }

    /**
     * This method disconnects the client from the RabbitMQ Server.
     */
    private void disconnect() throws IOException, TimeoutException {
        this.logShutdown("Disconnecting from RabbitMQ-Server...");
        this.channel.close();
        this.connection.close();
        this.logShutdown("Disconnection successful");
    }

    /**
     * This method is called during the JVM shutdown sequence.
     */
    private void shutdown() {
        try {
            this.beforeDisconnect();
            this.disconnect();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
        MyLogManager.resetFinally();
    }

    /**
     * This method creates a queue, binds it to an exchanges and links the callback.
     *
     * @param exchange   The exchange we want to bind the new queue to
     * @param callback   The callback used to consume from the queue
     * @param routingKey The routing queue used to bind the que to the exchange
     * @return The name of the created queue.
     */
    protected String subscribeToQueue(String exchange, DeliverCallback callback, String routingKey) throws IOException {
        String queueName = channel.queueDeclare().getQueue();
        if (routingKey == null) {
            routingKey = queueName;
        }
        channel.queueBind(queueName, exchange, routingKey);
        channel.basicConsume(queueName, true, callback, consumerTag -> {
        });

        return queueName;
    }

    /**
     * This method declares a non-durable exchange.
     *
     * @param name       The name of the exchange.
     * @param type       The type of the exchange.
     * @param autodelete Whether the exchange should autodelete or not.
     */
    protected void declareExchange(String name, BuiltinExchangeType type, boolean autodelete) throws IOException {
        channel.exchangeDeclare(name, type, false, autodelete, null);
        logger.info("  - '" + name + "' exchange declared");
    }

    /**
     * This method declares a direct exchange. In our application, direct exchanges always autodelete.
     *
     * @param name The name of the exchange.
     */
    protected void declareDirectExchange(String name) throws IOException {
        this.declareExchange(name, BuiltinExchangeType.DIRECT, true);
    }
}
