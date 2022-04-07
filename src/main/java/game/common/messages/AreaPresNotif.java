package game.common.messages;

import game.common.Point;

public class AreaPresNotif extends Message {
    private final Point position;
    private final NotificationType type;

    public AreaPresNotif(Point position, NotificationType type) {
        this.position = position;
        this.type = type;
    }

    public Point getPosition() {
        return position;
    }

    public NotificationType getType() {
        return type;
    }

    public static AreaPresNotif fromBytes(byte[] bytes) {
        return (AreaPresNotif) Message.fromBytes(bytes);
    }
}
