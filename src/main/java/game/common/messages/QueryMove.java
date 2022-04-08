package game.common.messages;

import game.common.Direction;

public class QueryMove extends Query {
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
