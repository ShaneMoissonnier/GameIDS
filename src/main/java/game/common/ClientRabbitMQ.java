package game.common;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public abstract class ClientRabbitMQ {
    protected static final String HOST = "localhost";

    protected Logger logger = null;

    protected Connection connection = null;
    protected Channel channel = null;

    public ClientRabbitMQ() {
    }

    private void setupLogger() {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s%n");
        System.setProperty("java.util.logging.manager", MyLogManager.class.getName());
        logger = Logger.getLogger(this.getClass().getName());
    }

    /**
     * This method is used to log during the shutdown hook.
     *
     * It is necessary to bypass the logger during shutdown because I did not manage to change the LogManager class
     * when running with Maven : the system property is always set too late. Because of this, if we want to log something
     * during shutdown when running with Maven, we have to use System.out instead
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

    private void connect() throws IOException, TimeoutException {
        logger.info("Connecting to RabbitMQ-Server...");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);

        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
        logger.info("Connection successful");
    }

    protected void beforeDisconnect() throws IOException {
    }

    private void disconnect() throws IOException, TimeoutException {
        this.logShutdown("Disconnecting from RabbitMQ-Server...");
        this.channel.close();
        this.connection.close();
        this.logShutdown("Disconnection successful");
    }

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
     * The actual body of the client.
     * <p>
     * This is where the input loop for the main chat client in console mode is, for example.
     */
    protected abstract void mainBody() throws IOException;

    /**
     * This method creates a queue, binds it to an exchanges and links the callback.
     *
     * @param exchange   The exchange we want to bind the new queue to
     * @param callback   The callback used to consume from the queue
     * @param routingKey The routing queue used to bind the que to the exchange
     */
    protected void subscribeToQueue(String exchange, DeliverCallback callback, String routingKey) throws IOException {
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, exchange, routingKey);
        channel.basicConsume(queueName, true, callback, consumerTag -> {
        });
    }

    /**
     * Same as {@link ClientRabbitMQ#subscribeToQueue(String, DeliverCallback, String)}, but without a routing key.
     */
    protected void subscribeToQueue(String exchange, DeliverCallback callback) throws IOException {
        this.subscribeToQueue(exchange, callback, "");
    }

    /**
     * This method creates the common queues (system and messages), binds them and subscribe to them.
     */
    protected abstract void subscribeToQueues() throws IOException;

    protected void declareExchange(String name, BuiltinExchangeType type) throws IOException {
        channel.exchangeDeclare(name, type, false, true, null);
    }

    protected abstract void setupExchanges() throws IOException;


    /**
     * The client's main method. It manages the flow of the client's execution.
     */
    protected void run() throws IOException, TimeoutException {
        this.setupLogger();
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        this.connect();

        this.setupLogger();
        this.setupExchanges();
        this.subscribeToQueues();
        this.mainBody();
    }
}