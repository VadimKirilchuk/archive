package lol.guesshero.server

import grails.plugins.springsecurity.Secured

@Secured(['ROLE_ADMIN'])
class PlayerController {

    static scaffold = Player

}
