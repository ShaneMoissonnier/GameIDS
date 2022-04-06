package game.player.gui.global;

import game.player.gui.widgets.ImageDisplay;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;

public class ImageLoader {

    private static ImageDisplay getImage(URL url) throws IOException {
        return new ImageDisplay(ImageIO.read(url));
    }

    public static ImageDisplay readImage(String resourceName){
        URL url = ImageLoader.class.getResource("/"+resourceName+".png");
        try {
            return getImage(url);
        } catch (IOException e) {
            System.exit(1);
        }
        return null;
    }
}
