package game.common.messages;

import game.common.boardModel.BoardModel;

public class BoardUpdate extends Message {
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
