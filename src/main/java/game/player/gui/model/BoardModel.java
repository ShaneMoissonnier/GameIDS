package game.player.gui.model;

import java.awt.Point;
import game.player.Player;

import java.util.ArrayList;

public class BoardModel {

    private final int row = 6;
    private final int column = 6;

    private Tile[][] grid;
    private ArrayList<Player> players;
    private boolean[][] tilesAvailables;

    public BoardModel() {
        this.players = new ArrayList<>();
        this.grid = new Tile[row][column];
        initBoard();
    }

    public int getRow() {
        return this.row;
    }

    public int getColumn() {
        return this.column;
    }

    public void initBoard() {
        for (int i = 0; i < this.row; i++) {
            for (int j = 0; j < this.column; j++) {
                this.grid[i][j] = new Tile(i,j);
            }
        }
    }

    public boolean hasToken(Point pos) {
        return this.grid[pos.x][pos.y].hasToken();
    }

    public Token getToken(Point pos) {
        return this.grid[pos.x][pos.y].getToken();
    }

    public void moveToken(Token token, Point endPos) {
        Point start = token.getTile().getPosition();
        this.grid[start.x][start.y].removeToken();
        this.grid[endPos.x][endPos.y].addToken(token);
    }

    public void moveToken(Point startPos, Point endPos) {
        if (!hasToken(startPos))
            return;
        Token token = this.grid[startPos.x][startPos.y].removeToken();
        this.grid[endPos.x][endPos.y].addToken(token);
    }

    public void putTokenOn(Token token, Point pos) {
        this.grid[pos.x][pos.y].addToken(token);
    }

    public void removeToken(Token token) {
        for (int i = 0; i < this.row; i++) {
            for (int j = 0; j < this.column; j++) {
                if (this.grid[i][j].getToken() == token){
                    this.grid[i][j].removeToken();
                }
            }
        }
    }

    public void resetTilesAvailables() {
        for (int i = 0; i < this.row; i++) {
            for (int j = 0; j < this.column; j++) {
                this.tilesAvailables[i][j] = true;
            }
        }
    }

    public void setTileAvailable(Tile tile, boolean status) {
        this.tilesAvailables[tile.getPosition().x][tile.getPosition().y] = status;
    }
}
