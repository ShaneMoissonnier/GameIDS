package game.common.messages;

public class QueryHello extends Query{

    private final PlayerInfos playerInfos;

    public QueryHello(String senderId, PlayerInfos playerInfos) {
        super(senderId);
        this.playerInfos = playerInfos;
    }

    public PlayerInfos getPlayerInfos() {
        return this.playerInfos;
    }
}
