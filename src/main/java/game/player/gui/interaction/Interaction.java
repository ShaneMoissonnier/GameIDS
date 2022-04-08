package game.player.gui.interaction;

import game.common.Direction;
import game.common.boardModel.BoardModel;
import game.player.gui.widgets.BoardDisplay;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Interaction implements KeyListener {
    private final BoardDisplay boardDisplay;

    public Interaction(BoardDisplay boardDisplay) {
        this.boardDisplay = boardDisplay;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        BoardModel model = this.boardDisplay.getModel();
        if (model == null) {
            // Not logged in
            return;
        }

        // No player's token on board
        if (model.getCurrentTokenPosition() == null)
            return;

        Direction direction;

        int keyCode = e.getKeyCode();
        switch( keyCode ) {
            case KeyEvent.VK_UP:
                direction = Direction.UP;
                break;
            case KeyEvent.VK_DOWN:
                direction = Direction.DOWN;
                break;
            case KeyEvent.VK_LEFT:
                direction = Direction.LEFT;
                break;
            case KeyEvent.VK_RIGHT :
                direction = Direction.RIGHT;
                break;
            default:
                return;
        }

        model.moveCurrentToken(direction);
        this.boardDisplay.repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {}
}
