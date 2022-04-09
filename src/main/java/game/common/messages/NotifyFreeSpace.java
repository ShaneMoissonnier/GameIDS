package game.common.messages;

import game.common.Point;

public class NotifyFreeSpace extends Message {
    private final Point areaPosition;
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
