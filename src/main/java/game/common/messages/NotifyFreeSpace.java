package game.common.messages;

import game.common.Point;

/**
 * This message class is used by area managers to notify the dispatcher when they become full or when some place clears up.
 * <p>
 * The dispatcher needs this information in order to only redirect players to areas that have enough space to receive them.
 */
public class NotifyFreeSpace extends Message {
    /**
     * The position of the area sending the notification.
     */
    private final Point areaPosition;

    /**
     * Whether the area is full or not.
     */
    private final boolean full;

    public NotifyFreeSpace(Point areaPosition, boolean full) {
        this.areaPosition = areaPosition;
        this.full = full;
    }

    public Point getAreaPosition() {
        return areaPosition;
    }

    public boolean isFull() {
        return full;
    }

    public static NotifyFreeSpace fromBytes(byte[] bytes) {
        return (NotifyFreeSpace) Message.fromBytes(bytes);
    }
}
