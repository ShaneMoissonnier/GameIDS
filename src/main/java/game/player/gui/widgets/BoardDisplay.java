package game.player.gui.widgets;
import game.player.gui.global.ImageLoader;
import game.player.gui.model.BoardModel;
import game.player.gui.model.Token;

import java.awt.*;
import java.util.ArrayList;

public class BoardDisplay extends Board {
    ImageDisplay playerToken;
    ImageDisplay tileGrass;

    private int columnCount;
    private int rowCount;
    private ArrayList<Rectangle> cells;

    public BoardDisplay() {
        this.boardModel = new BoardModel();
        this.rowCount = boardModel.getRow();
        this.columnCount = boardModel.getColumn();

        this.loadImages();

        this.cells = new ArrayList<>(columnCount * rowCount);

        this.testToken();
    }

    protected void loadImages() {
        this.playerToken = ImageLoader.readImage("token_penguin");
        this.tileGrass = ImageLoader.readImage("tileGrass");
    }

    public BoardModel getModel() { return this.boardModel; }

    @Override
    protected void updateBoard() {
        displayBoard();
        updateTokens();
    }

    protected void testToken() {
        Token token = new Token(Color.black, 60, this.playerToken);
        this.boardModel.setCurrentToken(token);
        this.boardModel.putTokenOn(token, new Point(0, 0));
        this.boardModel.moveToken(token, new Point(2, 4));
    }

    protected void updateTokens() {
        int width = getWidth();
        int height = getHeight();

        int cellWidth = width / columnCount;
        int cellHeight = height / rowCount;

        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < columnCount; col++) {

                int x = row * cellWidth;
                int y = col * cellHeight;

                Point position = new Point(row, col);

                if (this.boardModel.hasToken(position)) {
                    Token token = this.boardModel.getToken(position);
                    drawImage(token.getImage(), x + (cellWidth - token.getSize())/2, y + (cellHeight - token.getSize())/2, token.getSize(), token.getSize());
                }
            }
        }
    }

    protected void displayBoard() {
        int width = getWidth();
        int height = getHeight();

        int cellWidth = width / columnCount;
        int cellHeight = height / rowCount;

        int x = (width - (columnCount * cellWidth)) / 2;
        int y = (height - (rowCount * cellHeight)) / 2;

        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < columnCount; col++) {
                drawImage(this.tileGrass, x + (col * cellWidth), y + (row * cellHeight), cellWidth, cellHeight);
                drawEmptyRect(Color.DARK_GRAY,x + (col * cellWidth), y + (row * cellHeight), cellWidth, cellHeight, 10);
            }
        }
    }
}
