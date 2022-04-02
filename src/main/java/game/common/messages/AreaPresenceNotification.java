package game.common.messages;

import game.common.Point;

public class AreaPresenceNotification extends Message {
    private final Point senderCoordinates;
    private final AreaPresenceNotificationType type;

    private final Boolean isResponse;

    public AreaPresenceNotification(Point senderCoordinates, AreaPresenceNotificationType type, Boolean isResponse) {
        super(senderCoordinates.toString());
        this.senderCoordinates = senderCoordinates;
        this.type = type;
        this.isResponse = isResponse;
    }

    public static AreaPresenceNotification fromBytes(byte[] bytes) {
        return (AreaPresenceNotification) Message.fromBytes(bytes);
    }

    public Point getSenderCoordinates() {
        return senderCoordinates;
    }

    public AreaPresenceNotificationType getType() {
        return type;
    }

    public Boolean isResponse() {
        return isResponse;
    }
}
