package game.player.gui.model;

import java.awt.*;

public class Token {
    private Tile tile;
    private Color color;
    private int size = 50;

    public Token(Color color) {
        this.color = color;
    }

    public Token(Color color, int size) {
        this(color);
        this.size = size;
    }

    public int getSize() { return this.size; }

    public Color getColor() {
        return this.color;
    }

    public void setTile(Tile tile) {
        this.tile = tile;
    }

    public Tile getTile() {
        return this.tile;
    }
}
