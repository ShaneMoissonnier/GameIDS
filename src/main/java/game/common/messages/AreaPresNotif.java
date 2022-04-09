package game.common.messages;

import game.common.Point;

/**
 * This message class is used by the areas managers to notify their neighbors when they connect/disconnect.
 * <p>
 * It is also used to notify the Dispatcher when an area logs out.
 */
public class AreaPresNotif extends Message {
    /**
     * The position of the area sending the message.
     */
    private final Point position;

    /**
     * The type of the notification. Either LOGIN or LOGOUT.
     */
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
