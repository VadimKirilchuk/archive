package lol.guesshero.server.game;

import java.util.HashMap;
import java.util.Map;

import lol.guesshero.server.Player;

/**
 * Not thread safe!
 *  
 * @author vkirilchuk
 */
public class Game {
    
    private final Player creator; //TODO: make all equal
    private final Map<Player, GamePlayer> playersMap; 
    private final int playersLimit;
    
    public Game(Player creator, int playersLimit) {
        this.creator = creator;
        this.playersLimit = Math.max(2, playersLimit);
        this.playersMap = new HashMap<Player, GamePlayer>();
        addPlayer(creator);
    }
    
    private void addPlayer(Player candidate) {//TODO: maybe return GamePlayer?
        if (playersMap.size() < playersLimit) {
            GamePlayer gamePlayer = new GamePlayer(candidate);
            playersMap.put(candidate, gamePlayer);
        } else {
            throw new RuntimeException("full");
        }
    }
    
    private void removePlayer(Player candidate) {
        playersMap.remove(candidate);
    }
}
