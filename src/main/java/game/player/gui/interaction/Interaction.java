package game.player.gui.interaction;

import game.common.Direction;
import game.player.gui.model.BoardModel;
import game.player.gui.widgets.BoardDisplay;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Interaction implements KeyListener {
    private BoardDisplay boardDisplay;
    private BoardModel boardModel;

    public Interaction(BoardDisplay boardDisplay) {
        this.boardDisplay = boardDisplay;
        this.boardModel = this.boardDisplay.getModel();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        // No player's token on board
        if (this.boardModel.getCurrentToken() == null)
            return;

        int keyCode = e.getKeyCode();
        switch( keyCode ) {
            case KeyEvent.VK_UP:
                this.boardModel.moveTokenWithDirection(this.boardModel.getCurrentToken(), Direction.UP);
                break;
            case KeyEvent.VK_DOWN:
                this.boardModel.moveTokenWithDirection(this.boardModel.getCurrentToken(), Direction.DOWN);
                break;
            case KeyEvent.VK_LEFT:
                this.boardModel.moveTokenWithDirection(this.boardModel.getCurrentToken(), Direction.LEFT);
                break;
            case KeyEvent.VK_RIGHT :
                this.boardModel.moveTokenWithDirection(this.boardModel.getCurrentToken(), Direction.RIGHT);
                break;
        }
        this.boardDisplay.repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {}
}
