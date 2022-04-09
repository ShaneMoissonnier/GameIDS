package game.common.messages;

import game.common.Point;

/**
 * This class holds some information about a player.
 */
public class PlayerInfos extends Message {
    /**
     * The player's ID.
     */
    private final String playerId;

    /**
     * The player's position in their area.
     */
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
