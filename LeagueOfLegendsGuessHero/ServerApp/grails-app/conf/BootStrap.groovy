import grails.converters.JSON;
import grails.converters.XML;
import lol.guesshero.server.Player;
import lol.guesshero.server.game.Game;
import lol.guesshero.server.game.GameService;
import lol.guesshero.server.security.Role
import lol.guesshero.server.security.User
import lol.guesshero.server.security.UserRole

class BootStrap {

    GameService gameService

    def init = { servletContext ->
        initSecurity()
        initGameService()
    }

    def destroy = {
    }
       
    private void initGameService() {
        Player creator = new Player(username: 'Creator')
        Player player2 = new Player(username: 'Player2')
        long gameId = gameService.createGame(creator, 2)
        gameService.joinGame(gameId, player2)
        
        Player player3 = new Player(username: 'Player3')
        gameId = gameService.createGame(creator, 4)
        gameService.joinGame(gameId, player3)
    }

    private void initSecurity() {
        def userRole = Role.findByAuthority('ROLE_USER') ?: new Role(authority: 'ROLE_USER').save(failOnError: true)
        def adminRole = Role.findByAuthority('ROLE_ADMIN') ?: new Role(authority: 'ROLE_ADMIN').save(failOnError: true)

        def adminUser = User.findByUsername('admin') ?: new User(
                username: 'admin',
                password: 'admin',
                enabled: true).save(failOnError: true)

        if (!adminUser.authorities.contains(adminRole)) {
            UserRole.create(adminUser, adminRole)
        }
    }
}
