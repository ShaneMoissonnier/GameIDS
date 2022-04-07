package game.common.messages;

public class PlayerPresNotif extends Query {
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
