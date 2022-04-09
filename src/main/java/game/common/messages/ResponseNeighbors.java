package game.common.messages;

import game.common.Direction;
import game.common.Point;

import java.util.Map;

public class ResponseNeighbors extends Message{

    private final Point playerPosition;
    private final Map<Direction,PlayerInfos> neighbors;

    public ResponseNeighbors(Point playerPosition, Map<Direction, PlayerInfos> neighbors) {
        this.playerPosition = playerPosition;
        this.neighbors = neighbors;
    }

    public PlayerInfos getPlayerId(Direction direction) {
        return this.neighbors.get(direction);
    }
}
