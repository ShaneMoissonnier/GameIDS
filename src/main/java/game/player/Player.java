package game.player;

import game.common.ClientRabbitMQ;

public class Player extends ClientRabbitMQ {
    private final PlayerInfo playerInfo;

    public Player() {
        super();
        this.playerInfo = new PlayerInfo();
    }

    @Override
    protected void mainBody() {

    }

    @Override
    protected void subscribeToQueues() {

    }

    @Override
    protected void setupExchanges() {

    }

    public static void main(String[] args) {
        new Player();
    }
}
