package game.common.messages;

/**
 * This message class is used by players to ask their area managers for information about their neighbors.
 */
public class QueryNeighbors extends Query {
    public QueryNeighbors(String senderId) {
        super(senderId);
    }
}
