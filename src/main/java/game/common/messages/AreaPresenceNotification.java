package game.common.messages;

import game.common.Point;

public class AreaPresenceNotification extends Message {
    private final Point coordinates;
    private final AreaPresenceNotificationType type;

    private final Boolean isResponse;

    public AreaPresenceNotification(Point coordinates, AreaPresenceNotificationType type, Boolean isResponse) {
        super(coordinates.toString());
        this.coordinates = coordinates;
        this.type = type;
        this.isResponse = isResponse;
    }

    public static AreaPresenceNotification fromBytes(byte[] bytes) {
        return (AreaPresenceNotification) Message.fromBytes(bytes);
    }

    public Point getCoordinates() {
        return coordinates;
    }

    public AreaPresenceNotificationType getType() {
        return type;
    }

    public Boolean isResponse() {
        return isResponse;
    }
}
