package game.common.messages;

public class QueryPosition extends Message {
    public final SenderType type;

    public QueryPosition(String senderId, SenderType type) {
        super(senderId);
        this.type = type;
    }

    public static QueryPosition fromBytes(byte[] bytes) {
        return (QueryPosition) Message.fromBytes(bytes);
    }
}
