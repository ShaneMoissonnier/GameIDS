package game.common.messages;

public class QueryPosition extends Query {
    private final SenderType type;

    public QueryPosition(String senderId, SenderType type) {
        super(senderId);
        this.type = type;
    }

    public SenderType getType() {
        return type;
    }

    public static QueryPosition fromBytes(byte[] bytes) {
        return (QueryPosition) Message.fromBytes(bytes);
    }
}
