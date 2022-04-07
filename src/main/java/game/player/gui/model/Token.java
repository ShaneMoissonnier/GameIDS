package game.player.gui.model;

import game.player.gui.widgets.ImageDisplay;

import java.awt.*;

public class Token {
    private ImageDisplay image;
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

    public Token(Color color, int size, ImageDisplay image) {
        this(color, size);
        this.image = image;
    }

    public int getSize() { return this.size; }

    public ImageDisplay getImage() { return this.image; }

    public void setImage(ImageDisplay image) {
        this.image = image;
    }

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
