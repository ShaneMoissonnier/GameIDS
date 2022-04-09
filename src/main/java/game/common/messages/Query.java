package game.common.messages;

/**
 * This abstract class represents a query, i.e. a message that is meant to be replied to.
 * <p>
 * It holds an ID that is used as a routing key to respond to the message.
 */
public abstract class Query extends Message {
    /**
     * The routing key used to respond to the query.
     */
    private final String senderId;

    public Query(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderId() {
        return senderId;
    }
}
