package game.player.gui.global;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Global {

    private final ArrayList<String> skinList;
    public static Image tileGrass = ImageLoader.readImage("tileGrass");

    public Global() {
        skinList = new ArrayList<>();
        loadSkins();
    }

    private void loadSkins() {
        skinList.add("penguin");
        skinList.add("pig");
        skinList.add("snake");
        skinList.add("elephant");
        skinList.add("panda");
        skinList.add("monkey");
        skinList.add("giraffe");
        skinList.add("parrot");
        skinList.add("rabbit");
    }

    public String getRandomSkin() {
       int randomIndex = new Random().nextInt(this.skinList.size());
       return this.skinList.get(randomIndex);
    }

    public ArrayList<String> getSkinList() {
        return this.skinList;
    }
}
