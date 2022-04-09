package game.common.messages;

import game.common.boardModel.BoardModel;

/**
 * This message class is used by the area managers to notify their players that a change has taken place on the board.
 */
public class BoardUpdate extends Message {
    /**
     * The new state of the area's board.
     */
    private final BoardModel model;

    public BoardUpdate(BoardModel model) {
        this.model = model;
    }

    public BoardModel getModel() {
        return model;
    }

    public static BoardUpdate fromBytes(byte[] bytes) {
        return (BoardUpdate) Message.fromBytes(bytes);
    }
}
