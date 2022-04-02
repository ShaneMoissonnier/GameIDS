package game.common;

import java.io.Serializable;

public class Point implements Serializable {
    private final int row;
    private final int column;

    public Point(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return this.getRow() + ":" + this.getColumn();
    }

    public String toPlayerId(int id) {
        return this + ":" + id;
    }

    public Point getNeighbor(Direction direction) {
        switch (direction) {
            case UP:
                return new Point(row - 1, column);
            case LEFT:
                return new Point(row, column - 1);
            case DOWN:
                return new Point(row + 1, column);
            case RIGHT:
                return new Point(row, column + 1);
            default:
                throw new IllegalStateException();
        }
    }

    /* Return down -> other is under this */
    public Direction getDirectionOfNeighbor(Point other) {
        if (this.row > other.row) {
            return Direction.UP;
        } else if (this.row < other.row) {
            return Direction.DOWN;
        } else if (this.column > other.column) {
            return Direction.LEFT;
        } else {
            return Direction.RIGHT;
        }
    }
}
