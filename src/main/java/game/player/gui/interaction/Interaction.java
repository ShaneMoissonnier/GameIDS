package game.player.gui.interaction;

import game.player.gui.widgets.BoardDisplay;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Interaction implements KeyListener {
    private BoardDisplay boardDisplay;

    public Interaction(BoardDisplay boardDisplay) {
        this.boardDisplay = boardDisplay;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        switch( keyCode ) {
            case KeyEvent.VK_UP:
                this.boardDisplay.getModel().moveToken(new Point(2, 4), new Point(2, 3));
                this.boardDisplay.repaint();
                break;
            case KeyEvent.VK_DOWN:
                break;
            case KeyEvent.VK_LEFT:
                break;
            case KeyEvent.VK_RIGHT :
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
}
