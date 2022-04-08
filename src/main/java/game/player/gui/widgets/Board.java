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

    protected void drawCircle(Color c, int x, int y, int width, int height) {
        gPaint.setPaint(c);
        gPaint.fillOval(x, y, width, height);
    }

    protected void drawFullRect(Color c, int x, int y, int width, int heigth) {
        gPaint.setPaint(c);
        gPaint.fillRect(x, y, width, heigth);
    }

    protected void drawEmptyRect(Color borderColor, int x, int y, int width, int heigth)  {
        Rectangle cell = new Rectangle(x,y, width, heigth);
        gPaint.setColor(borderColor);
        gPaint.draw(cell);
    }

    protected void drawEmptyRect(Color borderColor, int x, int y, int width, int heigth, int thickness)  {
        Rectangle cell = new Rectangle(x,y, width, heigth);
        gPaint.setColor(borderColor);
        gPaint.setStroke(new BasicStroke(thickness));
        gPaint.draw(cell);
    }

    protected void setBackgroundColor(Color c) {
        gPaint.setBackground(c);
    }

    protected void drawText(String text, int x, int y, Color color) {
        gPaint.setColor(color);
        gPaint.drawString(text, x, y);
    }

    public void moveWithAnimation(Point position) {

    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        gPaint = (Graphics2D) g;
        this.setBackgroundColor(Color.DARK_GRAY);
        g.clearRect(0, 0, getWidth(), getHeight());
        this.updateBoard();
    }
}
