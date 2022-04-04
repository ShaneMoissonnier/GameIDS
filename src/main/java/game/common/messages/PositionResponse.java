package game.common.messages;

import game.common.Point;

public class PositionResponse extends Message {
    private final Point position;

    public PositionResponse(Point position) {
        super(null);
        this.position = position;
    }

    public Point getPosition() {
        return position;
    }

    public static PositionResponse fromBytes(byte[] bytes) {
        return (PositionResponse) Message.fromBytes(bytes);
    }
}
