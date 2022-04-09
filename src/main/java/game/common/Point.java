package game.common;

import java.io.Serializable;

/**
 * This class represents a position, either in the area grid or in an area itself.
 * <p>
 * While we call it point, it does not represent a geometrical point (with x and y coordinates), but indices in a 2D array.
 */
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

    /**
     * Returns the neighbor of this point in a given direction.
     *
     * @param direction The direction towards which the neighbor is.
     * @return The neighbor in a given direction.
     */
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

    /**
     * Given a neighbor point, this method return the direction of the neighbor, relative to this point.
     *
     * @param other A neighbor point
     * @return The direction of the neighbor relative to this point.
     */
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
