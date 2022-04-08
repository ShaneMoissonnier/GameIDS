package game.player.gui.widgets;

import game.common.Point;
import game.player.gui.global.Global;
import game.common.boardModel.BoardModel;
import game.common.boardModel.Token;

public class BoardDisplay extends Board {
    public BoardDisplay() {
    }

    public BoardModel getModel() {
        return this.boardModel;
    }

    @Override
    protected void updateBoard() {
        if (this.boardModel == null) {
            return;
        }

        int width = getWidth();
        int height = getHeight();

        int rowCount = this.boardModel.getRows();
        int columnCount = this.boardModel.getColumns();

        int cellWidth = width / columnCount;
        int cellHeight = height / rowCount;

        int x_grass = (width - (columnCount * cellWidth)) / 2;
        int y_grass = (height - (rowCount * cellHeight)) / 2;

        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < columnCount; col++) {
                drawImage(Global.tileGrass, x_grass + (col * cellWidth), y_grass + (row * cellHeight), cellWidth, cellHeight);
                drawEmptyRect(x_grass + (col * cellWidth), y_grass + (row * cellHeight), cellWidth, cellHeight);

                int x = col * cellWidth;
                int y = row * cellHeight;

                Point position = new Point(row, col);
                if (this.boardModel.hasToken(position)) {
                    Token token = this.boardModel.getToken(position);
                    drawImage(token.getImage(), x + (cellWidth - token.getSize()) / 2, y + (cellHeight - token.getSize()) / 2, token.getSize(), token.getSize());
                }
            }
        }
    }

    public void setBoardModel(BoardModel model) {
        this.boardModel = model;

        if (model != null) {
            Point position = model.getAreaPosition();
            HeaderPanel.setLabelText("(" + position.getRow() + ", " + position.getColumn() + ")");
        }

        this.repaint();
    }
}
