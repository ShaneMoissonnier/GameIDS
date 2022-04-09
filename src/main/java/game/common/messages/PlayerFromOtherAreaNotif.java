package game.common.messages;

import game.common.Point;
import game.common.boardModel.Token;

/**
 * This message class is used by players to let an area know that they are coming in from another area.
 * <p>
 * The areas react differently when a player comes in from another area than when they come in from the dispatcher, hence
 * the need for a different message class.
 *
 * @see PlayerPresNotif for when a player comes in from the dispatcher.
 */
public class PlayerFromOtherAreaNotif extends Query {
    /**
     * The player's token. It is used to maintain consistency in the player's appearance when going from an area to another
     */
    private final Token token;

    /**
     * The position the player is supposed to spawn at. It is determined according to the position the player was at
     * when leaving their previous area.
     */
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
