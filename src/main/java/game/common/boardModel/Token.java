package game.common.boardModel;

import game.player.gui.global.Global;
import game.player.gui.global.ImageLoader;

import java.awt.Image;
import java.io.Serializable;

public class Token implements Serializable {
    private final int size;
    private final String imageUrl;
    private transient Image image;

    public Token() {
        this.size = 60;
        this.imageUrl = new Global().getRandomSkin();
    }

    public int getSize() {
        return this.size;
    }

    public Image getImage() {
        if (this.image == null) {
            this.image = ImageLoader.readImage(this.imageUrl);
        }
        return this.image;
    }
}
