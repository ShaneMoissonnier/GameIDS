package game.common.messages;

public class ResponseMove extends Message {
    private final boolean status;

    public ResponseMove(boolean status) {
        this.status = status;
    }

    public boolean isStatus() {
        return status;
    }

    public static ResponseMove fromBytes(byte[] bytes) {
        return (ResponseMove) Message.fromBytes(bytes);
    }
}
