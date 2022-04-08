package game.common.messages;

import game.common.Point;
import game.common.boardModel.Token;

public class PlayerFromOtherAreaNotif extends Query {
    private final Token token;
    private final Point position;

    public PlayerFromOtherAreaNotif(String senderId, Token token, Point position) {
        super(senderId);
        this.token = token;
        this.position = position;
    }

    public Token getToken() {
        return token;
    }

    public Point getPosition() {
        return position;
    }

    public static PlayerFromOtherAreaNotif fromBytes(byte[] bytes) {
        return (PlayerFromOtherAreaNotif) Message.fromBytes(bytes);
    }
}
