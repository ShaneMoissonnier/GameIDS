package game.common.messages;

import game.common.Point;

public class ResponseMove extends Message {
    private final boolean status;
    private final Point position;

    public ResponseMove(boolean status, Point position) {
        this.status = status;
        this.position = position;
    }

    public boolean isStatus() {
        return status;
    }

    public static ResponseMove fromBytes(byte[] bytes) {
        return (ResponseMove) Message.fromBytes(bytes);
    }
}
