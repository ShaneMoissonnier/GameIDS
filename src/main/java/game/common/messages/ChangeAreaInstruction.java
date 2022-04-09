package game.common.messages;

import game.common.Point;

/**
 * This message class is used by an area manager to let a player know that they must change the area manager they
 * subscribe to.
 * <p>
 * It is used when a player tries to move from an area to another. Let's say a player is on the rightmost column of the
 * area A, and the area B is on the right of A.
 * If the player tries to move one tile right, the area A will send a message to the player containing the position of
 * the area B, and the position they will spawn at in the area B.
 */
public class ChangeAreaInstruction extends Message {
    /**
     * The position of the area the player will end up in.
     */
    private final Point areaPosition;

    /**
     * The position in the destination area the player will spawn at.
     */
    private final Point playerPosition;

    public ChangeAreaInstruction(Point areaPosition, Point playerPosition) {
        this.areaPosition = areaPosition;
        this.playerPosition = playerPosition;
    }

    public Point getAreaPosition() {
        return areaPosition;
    }

    public Point getPlayerPosition() {
        return playerPosition;
    }

    public static ChangeAreaInstruction fromBytes(byte[] bytes) {
        return (ChangeAreaInstruction) Message.fromBytes(bytes);
    }
}
