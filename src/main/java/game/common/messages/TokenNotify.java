package game.common.messages;

import game.common.boardModel.Token;

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
