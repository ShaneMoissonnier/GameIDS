package game.common.messages;

import com.rabbitmq.client.Delivery;

/**
 * This message class is used by the players and the area managers to request a "position" from the dispatcher.
 * <p>
 * If the requester is an area, the dispatcher will give them a free position in the "area grid".
 * If the requester is a player, the dispatcher will give them the position of a random area for them to connect to.
 *
 * @see game.Dispatcher#queryPositionCallback(String, Delivery) for more details.
 */
public class QueryPosition extends Query {
    /**
     * The type of the requester. Either PLAYER or AREA.
     */
    private final SenderType type;

    public QueryPosition(String senderId, SenderType type) {
        super(senderId);
        this.type = type;
    }

    public SenderType getType() {
        return type;
    }

    public static QueryPosition fromBytes(byte[] bytes) {
        return (QueryPosition) Message.fromBytes(bytes);
    }
}
