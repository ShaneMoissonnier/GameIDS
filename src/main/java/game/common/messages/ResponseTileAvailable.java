package game.common.messages;

import game.common.Point;

public class ResponseTileAvailable extends Message {
    private final String id;
    private final Point areaPosition;
    private final Point tile;
    private final boolean status;

    public ResponseTileAvailable(String id, Point areaPosition, Point tile, boolean status) {
        this.id = id;
        this.areaPosition = areaPosition;
        this.tile = tile;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public Point getAreaPosition() {
        return areaPosition;
    }

    public Point getTile() {
        return tile;
    }

    public boolean getStatus() {
        return status;
    }

    public static ResponseTileAvailable fromBytes(byte[] bytes) {
        return (ResponseTileAvailable) Message.fromBytes(bytes);
    }
}
