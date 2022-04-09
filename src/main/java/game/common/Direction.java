package game.common;

public enum Direction {
    UP,
    LEFT,
    DOWN,
    RIGHT;

    public Direction getOpposite() {
        switch (this) {
            case UP:
                return Direction.DOWN;
            case DOWN:
                return Direction.UP;
            case LEFT:
                return Direction.RIGHT;
            case RIGHT:
                return Direction.LEFT;
            default:
                throw new IllegalStateException();
        }
    }
}
