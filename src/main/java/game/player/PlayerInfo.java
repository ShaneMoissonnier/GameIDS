package game.player;

import game.common.Point;

public class PlayerInfo {
    private String id;
    private Point position;

    public PlayerInfo() {
        this.position = new Point(0, 0);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public Point getPosition() {
        return this.position;
    }
}
