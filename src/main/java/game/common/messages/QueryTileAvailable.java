package game.common.messages;

import game.common.Direction;
import game.common.Point;

/**
 * This message class is used by the area managers to ask other managers if a specific tile is free or not.
 * <p>
 * This is used when a player tries to move from an area to another, to check if the move is possible.
 */
public class QueryTileAvailable extends Query {
    /**
     * The tile we want to check.
     */
    private final Point position;

    /**
     * The direction the sender is, relative to the recipient of the query. It is used to know which exchange to use
     * when replying.
     */
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
