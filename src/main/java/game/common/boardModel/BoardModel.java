package game.common.boardModel;

import game.common.Direction;
import game.common.Point;

import java.io.Serializable;

public class BoardModel implements Serializable {
    private final int rows = 6;
    private final int columns = 6;

    private final Token[][] grid;
    private Point currentTokenPosition;

    public BoardModel() {
        this.grid = new Token[rows][columns];
        initBoard();
    }

    public int getRows() {
        return this.rows;
    }

    public int getColumns() {
        return this.columns;
    }

    public void initBoard() {
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.columns; j++) {
                this.grid[i][j] = null;
            }
        }
    }

    public boolean isOutbounds(Point position) {
        return (position.getRow() >= this.getRows() || position.getRow() < 0 || position.getColumn() >= this.getColumns() || position.getColumn() < 0);
    }

    public boolean hasToken(Point pos) {
        return this.grid[pos.getRow()][pos.getColumn()] != null;
    }

    public Token getToken(Point pos) {
        return this.grid[pos.getRow()][pos.getColumn()];
    }

    public void moveCurrentToken(Direction direction) {
        Point dest = this.currentTokenPosition.getNeighbor(direction);

        // TODO add manager verification
        if (this.isOutbounds(dest)) {
            return;
        }

        Token token = this.getToken(this.currentTokenPosition);
        this.putTokenOn(token, dest);
        this.removeTokenAt(this.currentTokenPosition);
        this.currentTokenPosition = dest;
    }

    public void putTokenOn(Token token, Point pos) {
        this.grid[pos.getRow()][pos.getColumn()] = token;
    }

    public void setCurrentTokenPosition(Point position) {
        this.currentTokenPosition = position;
    }

    public Point getCurrentTokenPosition() {
        return this.currentTokenPosition;
    }

    public void removeTokenAt(Point position) {
        this.grid[position.getRow()][position.getColumn()] = null;
    }
}
