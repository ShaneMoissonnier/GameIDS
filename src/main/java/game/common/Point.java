package game.common;

import game.areaManager.Direction;

public class Point extends java.awt.Point {
    public Point(int x, int y) {
        super(x, y);
    }

    @Override
    public String toString() {
        return this.getX() + ":" + this.getY();
    }

    public String toPlayerId(int id) {
        return this + ":" + id;
    }

    public Point getNeighbor(Direction direction) {
        return switch (direction) {
            case UP -> new Point(x, y - 1);
            case LEFT -> new Point(x - 1, y);
            case DOWN -> new Point(x, y + 1);
            case RIGHT -> new Point(x + 1, y);
        };
    }
}
