package game.common.messages;

import game.common.Direction;

/**
 * This message class is used by the player to notify their area manager that they wish to move in a given direction.
 */
public class QueryMove extends Query {
    /**
     * The direction the player is trying to move towards.
     */
    private final Direction direction;

    public QueryMove(String senderId, Direction direction) {
        super(senderId);
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }

    public static QueryMove fromBytes(byte[] bytes) {
        return (QueryMove) Message.fromBytes(bytes);
    }
}
