package game.common.messages;

import game.common.Direction;
import game.common.Point;

public class QueryMove extends Query {
    private final Point position;
    private final Direction direction;

    public QueryMove(String senderId, Point position, Direction direction) {
        super(senderId);
        this.position = position;
        this.direction = direction;
    }

    public Point getPosition() {
        return position;
    }

    public Direction getDirection() {
        return direction;
    }

    public static QueryMove fromBytes(byte[] bytes) {
        return (QueryMove) Message.fromBytes(bytes);
    }
}
