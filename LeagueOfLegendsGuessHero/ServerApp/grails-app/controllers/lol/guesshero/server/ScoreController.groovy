package lol.guesshero.server

import grails.plugins.springsecurity.Secured

@Secured(['ROLE_ADMIN'])
class ScoreController {
    
    static scaffold = Score
}
