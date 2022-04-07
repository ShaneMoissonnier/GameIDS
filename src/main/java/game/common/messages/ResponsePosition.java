package game.common.messages;

import game.common.Point;

public class ResponsePosition extends Message {
    private final Point position;

    public ResponsePosition(Point position) {
        this.position = position;
    }

    public Point getPosition() {
        return position;
    }

    public static ResponsePosition fromBytes(byte[] bytes) {
        return (ResponsePosition) Message.fromBytes(bytes);
    }
}
