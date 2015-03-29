package lol.guesshero.server.game;

import lol.guesshero.server.Player;

public class GamePlayer {
    
    private final Player player;
    private int hp;
    private int mana;
    
    public GamePlayer(Player player) {
        this.player = player;
        this.hp = 100;
        this.mana = 100;
    }
}
