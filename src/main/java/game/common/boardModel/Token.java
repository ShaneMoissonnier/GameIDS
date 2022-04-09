package game.common.boardModel;

import game.player.gui.global.Global;
import game.player.gui.global.ImageLoader;

import java.awt.Image;
import java.io.Serializable;

/**
 * This class represents a Token, a representation of a player displayed on the board.
 */
public class Token implements Serializable {
    /**
     * The size of the token.
     */
    private final int size;

    /**
     * The URL pointing to the image of the token in the resource folder.
     */
    private final String imageUrl;

    /**
     * The image representing the token. This is what will be drawn in the UI.
     * <p>
     * We need to serialize the tokens to send them over to their players, but the {@link Image} class is not
     * serializable, meaning that this field needs to be transient. This is why we also store the {@link #imageUrl}, so
     * that once deserialized, we can find the image again.
     */
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
