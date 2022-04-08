package game.player.gui.global;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

public class ImageLoader {
    private static Image getImage(URL url) throws IOException {
        return ImageIO.read(url);
    }

    public static Image readImage(String resourceName){
        URL url = ImageLoader.class.getResource("/"+resourceName+".png");
        try {
            return getImage(url);
        } catch (IOException e) {
            System.exit(1);
        }
        return null;
    }
}
