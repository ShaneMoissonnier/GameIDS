package game.common.messages;

import game.common.Point;

public class ResponseAreaMove extends Message {
    private final Point areaPosition;
    private final Point playerPosition;

    public ResponseAreaMove(Point areaPosition, Point playerPosition) {
        this.areaPosition = areaPosition;
        this.playerPosition = playerPosition;
    }

    public Point getAreaPosition() {
        return areaPosition;
    }

    public Point getPlayerPosition() {
        return playerPosition;
    }

    public static ResponseAreaMove fromBytes(byte[] bytes) {
        return (ResponseAreaMove) Message.fromBytes(bytes);
    }
}
