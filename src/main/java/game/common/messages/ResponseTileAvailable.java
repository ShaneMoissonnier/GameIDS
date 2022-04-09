package game.common.messages;

import game.common.Point;

/**
 * This message class is used by an area to reply to a {@link QueryTileAvailable}.
 * <p>
 * This class holds a lot of information that are not exactly related to the query, but that are necessary for the
 * requester to correctly propagate the response to the player.
 */
public class ResponseTileAvailable extends Message {
    /**
     * The ID of the player that is trying to move to the area.
     */
    private final String id;

    /**
     * The area the player is trying to move to.
     */
    private final Point areaPosition;

    /**
     * The tile the player is trying to move to.
     */
    private final Point tile;

    /**
     * Whether the tile is free or not.
     */
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
