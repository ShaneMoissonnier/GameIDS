package game.common.messages;

import game.common.Direction;
import game.common.Point;

import java.util.Map;

/**
 * This message class is used to transmit information about the neighbors to a player.
 */
public class ResponseNeighbors extends Message {
    /**
     * The position of the requesting player.
     */
    private final Point playerPosition;

    /**
     * The information about the neighbors.
     */
    private final Map<Direction, PlayerInfos> neighbors;

    public ResponseNeighbors(Point playerPosition, Map<Direction, PlayerInfos> neighbors) {
        this.playerPosition = playerPosition;
        this.neighbors = neighbors;
    }

    public Point getPlayerPosition() {
        return playerPosition;
    }

    public PlayerInfos getNeighborInfo(Direction direction) {
        return this.neighbors.get(direction);
    }

    public static ResponseNeighbors fromBytes(byte[] bytes) {
        return (ResponseNeighbors) Message.fromBytes(bytes);
    }
}
