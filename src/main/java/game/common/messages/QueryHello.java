package game.common.messages;

/**
 * This message class is used to say hello to another player.
 */
public class QueryHello extends Query {
    /**
     * The info of the player saying hello.
     */
    private final PlayerInfos playerInfos;

    public QueryHello(String senderId, PlayerInfos playerInfos) {
        super(senderId);
        this.playerInfos = playerInfos;
    }

    public PlayerInfos getPlayerInfos() {
        return this.playerInfos;
    }
}
