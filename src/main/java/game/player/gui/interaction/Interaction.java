package game.player.gui.interaction;

import game.common.Direction;
import game.common.boardModel.BoardModel;
import game.player.Player;
import game.player.gui.widgets.BoardDisplay;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

public class Interaction implements KeyListener {
    private final Player player;
    private final BoardDisplay boardDisplay;

    public Interaction(BoardDisplay boardDisplay, Player player) {
        this.boardDisplay = boardDisplay;
        this.player = player;
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
            case KeyEvent.VK_SPACE:
                try {
                    this.player.trySayHello();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                return;
            default:
                return;
        }

        try {
            this.player.tryToMove(direction);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
}
