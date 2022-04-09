package game.common.messages;

import game.common.Point;

/**
 * This message class is used to transmit a position to an entity.
 *
 * It is used by the dispatcher to reply to {@link QueryPosition} type messages.
 */
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
