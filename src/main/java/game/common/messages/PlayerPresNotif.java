package game.common.messages;

/**
 * This message class is used by players to let an area know that they are coming in from the dispatcher.
 * <p>
 * The areas react differently when a player comes in from another area than when they come in from the dispatcher, hence
 * the need for a different message class.
 *
 * @see PlayerFromOtherAreaNotif for when a player comes in from another area.
 */
public class PlayerPresNotif extends Query {
    /**
     * The type of the notification. Either LOGIN or LOGOUT.
     */
    private final NotificationType type;

    public PlayerPresNotif(String senderId, NotificationType type) {
        super(senderId);
        this.type = type;
    }

    public NotificationType getType() {
        return type;
    }

    public static PlayerPresNotif fromBytes(byte[] bytes) {
        return (PlayerPresNotif) Message.fromBytes(bytes);
    }
}
