package game.areaManager;

import com.rabbitmq.client.Channel;
import game.common.Point;
import game.common.messages.NotifyFreeSpace;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class holds the position of each player identified by an ID in the area.
 */
public class PlayerMap {
    private final Channel channel;

    /**
     * The position of the area using this player map.
     */
    private Point areaPosition;

    /**
     * The maximum number of players this map can hold.
     */
    private final int max_size;

    /**
     * The mapping from ID to position of the players in the area.
     */
    private final Map<String, Point> players;

    public PlayerMap(Channel channel, int rows, int columns) {
        this.channel = channel;
        this.max_size = rows * columns;
        this.players = new HashMap<>();
    }

    public void put(String key, Point value) throws IOException {
        this.players.put(key, value);

        if (this.players.size() == max_size) {
            /* The area is full => We notify the dispatcher so that it does not send new players in this area. */
            NotifyFreeSpace msg = new NotifyFreeSpace(areaPosition, true);
            this.channel.basicPublish("dispatcher", "dispatcher_notify_free_space", null, msg.toBytes());
        }
    }

    public void remove(String key) throws IOException {
        this.players.remove(key);

        if (this.players.size() == max_size - 1) {
            /* We removed an element and size is now max_size - 1 => The map was full but is not anymore.
             * We notify the dispatcher so that it can send new players in this area if it so decides. */
            NotifyFreeSpace msg = new NotifyFreeSpace(areaPosition, false);
            this.channel.basicPublish("dispatcher", "dispatcher_notify_free_space", null, msg.toBytes());
        }
    }

    public Point get(String key) {
        return this.players.get(key);
    }

    public Set<Map.Entry<String, Point>> entrySet() {
        return this.players.entrySet();
    }

    public void setAreaPosition(Point areaPosition) {
        this.areaPosition = areaPosition;
    }
}
