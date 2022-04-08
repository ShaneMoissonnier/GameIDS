package game.common.boardModel;

import game.common.Point;

import java.io.Serializable;

public class BoardModel implements Serializable {
    private Point areaPosition;

    private final int rows = 6;
    private final int columns = 6;

    private final Token[][] grid;

    public BoardModel() {
        this.grid = new Token[rows][columns];
        initBoard();
    }

    public void setAreaPosition(Point areaPosition) {
        this.areaPosition = areaPosition;
    }

    public Point getAreaPosition() {
        return areaPosition;
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

    public boolean isTileAvailable(Point pos) {
        return !this.hasToken(pos);
    }

    public Token getToken(Point pos) {
        return this.grid[pos.getRow()][pos.getColumn()];
    }

    public void putTokenOn(Token token, Point pos) {
        this.grid[pos.getRow()][pos.getColumn()] = token;
    }

    public void removeTokenAt(Point position) {
        this.grid[position.getRow()][position.getColumn()] = null;
    }

    public void moveToken(Point start, Point dest) {
        Token token = this.getToken(start);
        this.removeTokenAt(start);
        this.putTokenOn(token, dest);
    }
}
