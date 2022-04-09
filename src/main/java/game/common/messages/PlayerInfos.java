package game.common.messages;

import game.common.Point;

public class PlayerInfos extends Message {

    private final String playerId;
    private final Point playerPosition;

    public PlayerInfos(String playerId, Point playerPosition) {
        this.playerId = playerId;
        this.playerPosition = playerPosition;
    }

    public String getPlayerId() {
        return this.playerId;
    }

    public Point getPlayerPosition() {
        return this.playerPosition;
    }
}
