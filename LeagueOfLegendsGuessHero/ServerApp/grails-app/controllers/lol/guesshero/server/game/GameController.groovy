package lol.guesshero.server.game

import org.springframework.context.MessageSource;

import lol.guesshero.server.Player;
import grails.converters.XML
import grails.plugins.springsecurity.Secured
import grails.plugins.springsecurity.SpringSecurityService;
import groovy.xml.MarkupBuilder

@Secured(['ROLE_PLAYER'])
class GameController {

    GameService gameService
    SpringSecurityService springSecurityService
    MessageSource messageSource

    @Secured(['ROLE_ADMIN', 'ROLE_PLAYER'])
    def list() {
        def allGames = gameService.allGames
        def writer = new StringWriter ()
        def xml = new MarkupBuilder(writer)
        xml.games() {
            allGames.each { entry ->
                Integer gameId = entry.key
                Game game = entry.value
                gameToXml(xml, gameId, game)
            }
        }
        render(contentType: 'text/xml', text: writer)
    }
    
    def create(int playerLimit) {
        if(!hasErrors()) {
            Player player = getCurrentPlayer()
            long gameId = gameService.createGame(player, playerLimit)
            render(contentType: 'text/xml') {
                delegate.gameId(gameId)
            }
        } else {
            response.status = 400
            render errors.allErrors.collect {
                message(error:messageSource.getMessage(it, request.locale))
            } as XML
        }
    }
    
    def join(long gameId) {
        if(!hasErrors()) {
            Player player = getCurrentPlayer()
            gameService.joinGame(gameId, player)    
        }
    }

    def leave(long gameId) {
        if(!hasErrors()) {
            Player player = getCurrentPlayer()
            gameService.leaveGame(gameId, player)
        }
    }

    private Player getCurrentPlayer() {
        return springSecurityService.currentUser
    }

    private void gameToXml(builder, Integer gameId, Game game) {
        builder.game(id: gameId, maxPlayers: game.playersLimit) {
            builder.players() {
                game.playersMap.keySet().each { player ->
                    playerToXml(builder, player, game.creator == player)
                }
            }
        }
    }

    private void playerToXml(builder, Player player, boolean creator) {
        builder.player(creator: creator) { username(player.username) }
    }
}
