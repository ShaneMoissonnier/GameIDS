package game.common.messages;

import game.common.Direction;
import game.common.Point;

public class QueryTileAvailable extends Query {
    private final Point position;
    private final Direction senderDirection;

    public QueryTileAvailable(String senderId, Point position, Direction senderDirection) {
        super(senderId);
        this.position = position;
        this.senderDirection = senderDirection;
    }

    public Point getPosition() {
        return position;
    }

    public Direction getSenderDirection() {
        return senderDirection;
    }

    public static QueryTileAvailable fromBytes(byte[] bytes) {
        return (QueryTileAvailable) Message.fromBytes(bytes);
    }
}
