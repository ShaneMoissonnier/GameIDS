package game.player.gui.global;

import java.util.ArrayList;
import java.util.Random;

public class Global {

    private ArrayList<ImageDisplay> skinList;
    public static ImageDisplay tileGrass = ImageLoader.readImage("tileGrass");

    public Global() {
        skinList = new ArrayList<>();
        loadSkins();
    }

    private void loadSkins() {
        skinList.add(ImageLoader.readImage("penguin"));
        skinList.add(ImageLoader.readImage("pig"));
        skinList.add(ImageLoader.readImage("snake"));
        skinList.add(ImageLoader.readImage("elephant"));
        skinList.add(ImageLoader.readImage("panda"));
        skinList.add(ImageLoader.readImage("monkey"));
        skinList.add(ImageLoader.readImage("giraffe"));
        skinList.add(ImageLoader.readImage("parrot"));
        skinList.add(ImageLoader.readImage("rabbit"));
    }

    public ImageDisplay getRandomSkin() {
       int randomIndex = new Random().nextInt(this.skinList.size());
       return this.skinList.get(randomIndex);
    }

    public ArrayList<ImageDisplay> getSkinList() {
        return this.skinList;
    }
}
