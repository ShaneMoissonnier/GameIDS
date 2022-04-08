package game.player.gui.widgets;

import javax.swing.*;
import java.awt.*;

import game.common.boardModel.BoardModel;

public abstract class Board extends JComponent {

    public BoardModel boardModel;
    private Graphics2D gPaint;

    abstract void updateBoard();

    protected void drawImage(Image img, int x, int y, int width, int height) {
        gPaint.drawImage(img, x, y, width, height, null);
    }

    protected void drawEmptyRect(int x, int y, int width, int height)  {
        Rectangle cell = new Rectangle(x,y, width, height);
        gPaint.setColor(Color.DARK_GRAY);
        gPaint.setStroke(new BasicStroke(10));
        gPaint.draw(cell);
    }

    protected void setBackgroundColor() {
        gPaint.setBackground(Color.DARK_GRAY);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        gPaint = (Graphics2D) g;
        this.setBackgroundColor();
        g.clearRect(0, 0, getWidth(), getHeight());
        this.updateBoard();
    }
}
