package game.common.messages;

import game.common.boardModel.Token;

/**
 * This message class is used by the area manager to transmit a token to a player.
 * <p>
 * The player needs its token to transmit it to an area when they are moving from an area to another.
 */
public class TokenNotify extends Message {
    private final Token token;

    public TokenNotify(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    public static TokenNotify fromBytes(byte[] bytes) {
        return (TokenNotify) Message.fromBytes(bytes);
    }
}
