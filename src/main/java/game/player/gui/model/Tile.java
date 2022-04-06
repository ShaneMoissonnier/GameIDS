package game.player.gui.model;

import java.awt.Point;

public class Tile {

    private Point position;
    private Token token;

    public Tile(int x, int y) {
        this.position = new Point(x, y);
    }

    public boolean hasToken() {
        return !(this.token == null);
    }

    public Token getToken() {
        return this.token;
    }

    public Token removeToken() {
        Token t = this.token;
        this.token = null;
        return t;
    }

    public void addToken(Token token) {
        token.setTile(this);
        this.token = token;
    }

    public Point getPosition() {
        return this.position;
    }
}
