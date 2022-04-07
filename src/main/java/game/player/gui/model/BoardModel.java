package game.player.gui.model;

import java.awt.Point;

import game.player.Directions;
import game.player.Player;

import java.util.ArrayList;
import java.util.Arrays;

public class BoardModel {

    private final int row = 6;
    private final int column = 6;

    private Tile[][] grid;
    private ArrayList<Player> players;
    private boolean[][] tilesAvailables;
    private Token currentToken;

    public BoardModel() {
        this.players = new ArrayList<>();
        this.grid = new Tile[row][column];
        this.tilesAvailables = new boolean[row][column];
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

    // TODO: to remove, only for debug
    public void showAvailablesTiles() {
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                System.out.println(tilesAvailables[i][j]);
            }
        }
    }

    public boolean isOutbounds(Point position) {
        return  (position.x >= this.getRow() || position.x < 0 || position.y >= this.getColumn() || position.y < 0);
    }

    public boolean hasToken(Point pos) {
        return this.grid[pos.x][pos.y].hasToken();
    }

    public Token getToken(Point pos) {
        return this.grid[pos.x][pos.y].getToken();
    }

    public void moveToken(Token token, Point endPos) {
        // TODO : replace by manager verification
        if (isOutbounds(endPos))
            return;
        Tile startTile = token.getTile();
        Tile endTile = this.grid[endPos.x][endPos.y];

        Point start = startTile.getPosition();
        startTile.removeToken();
        endTile.addToken(token);

        setTileAvailable(startTile, true);
        setTileAvailable(endTile, false);
    }

    public void moveToken(Point startPos, Point endPos) {
        if (!hasToken(startPos))
            return;
        Token token = this.grid[startPos.x][startPos.y].removeToken();
        this.grid[endPos.x][endPos.y].addToken(token);
    }

    public void moveTokenWithDirection(Token token, Directions directions) {

        Point currentPosition = (Point) token.getTile().getPosition().clone();

        switch (directions) {
            case NORTH:
                currentPosition.y -= 1;
                break;
            case SOUTH:
                currentPosition.y += 1;
                break;
            case WEST:
                currentPosition.x -= 1;
                break;
            case EAST:
                currentPosition.x += 1;
                break;
        }
        moveToken(token, currentPosition);
    }

    public void putTokenOn(Token token, Point pos) {
        Tile tile = this.grid[pos.x][pos.y];
        tile.addToken(token);
        setTileAvailable(tile, false);
    }

    public void setCurrentToken(Token token) {
        this.currentToken = token;
    }

    public Token getCurrentToken() {
        return this.currentToken;
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
