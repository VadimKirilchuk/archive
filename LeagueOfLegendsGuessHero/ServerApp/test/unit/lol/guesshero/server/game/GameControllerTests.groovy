package lol.guesshero.server.game



import grails.plugins.springsecurity.SpringSecurityService;
import grails.test.mixin.*
import lol.guesshero.server.Player

import org.junit.*

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(GameController)
class GameControllerTests {

    void testGameListGamesCount() {
        Map<Integer, Game> allGames = new HashMap<Integer, Game>();
        allGames.put(1, new Game(new Player(), 2))
        allGames.put(2, new Game(new Player(), 2))
        
        def gameServiceControl = mockFor(GameService)
        gameServiceControl.demand.getAllGames() { -> return allGames.entrySet()}
        controller.gameService = gameServiceControl.createMock()
        
        controller.list()
        
        def games = new XmlParser().parseText(response.text)
        assertEquals("Number of games in output and data should be the same", allGames.size(), games.game.size())
    }
    
    void testCreateGameOk() {
        long expectedGameId = 1000;
        
        def gameServiceControl = mockFor(GameService)
        gameServiceControl.demand.createGame() { creator, limit -> return expectedGameId}
        controller.gameService = gameServiceControl.createMock()
        
        Player currentUser = new Player()
        
        def securityControl = mockFor(SpringSecurityService)
        securityControl.demand.getCurrentUser() { -> return currentUser}
        controller.springSecurityService = securityControl.createMock()
        
        controller.create(10)
        
        def answer = new XmlParser().parseText(response.text)
        assertEquals(expectedGameId, answer.text() as long)
    }
    
    void testCreateGameWrongParam() {
        messageSource.addMessage("params.playerLimit.conversion.error", request.locale, "Wrong playerLimit")
        controller.messageSource = messageSource
        
        controller.params.playerLimit = "String"
        controller.create()
        
        assertEquals(400, response.status)
        def answer = new XmlParser().parseText(response.text)
        assertEquals("", response.text)
    }
}
