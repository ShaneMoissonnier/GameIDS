package game.common.messages;

public abstract class Query extends Message {
    private final String senderId; /* Used as a routing key to respond to the message */

    public Query(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderId() {
        return senderId;
    }
}
