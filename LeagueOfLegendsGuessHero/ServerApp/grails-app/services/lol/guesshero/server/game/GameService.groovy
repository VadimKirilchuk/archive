package lol.guesshero.server.game

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

import lol.guesshero.server.Player

class GameService {//TODO: think about not mixing Player and GamePlayer as they are separate concepts 

    /**
     * As this service is a global games storage.
     * Note: take care about thread safety.
     */
    static scope = "singleton"
    
    private Map<Long, Game> games = new ConcurrentHashMap<Long, Game>()
    private AtomicLong counter = new AtomicLong()
    
    def getAllGames() {
        return games.entrySet()
    }
    
    def createGame(Player creator, int playerLimit) {
        Long gameId = counter.incrementAndGet()
        Game game = new Game(creator, playerLimit)
        games.put(gameId, game);
        return gameId
    }
    
    def joinGame(long gameId, Player candidate) {
        Game game = getGame(gameId)
        synchronized (game) { //todo: can throw limit exception
            game.addPlayer(candidate)
        }
    }

    def leaveGame(long gameId, Player leaver) {
        Game game = getGame(gameId)
        synchronized (game) {
            game.removePlayer(leaver)
        }
    }
    
	private Game getGame(long gameId) {
		Game game = games.get(gameId)
		if (game == null) {
			throw new RuntimeException("Game not found") //TODO
		}
		return game
	}
    
}
