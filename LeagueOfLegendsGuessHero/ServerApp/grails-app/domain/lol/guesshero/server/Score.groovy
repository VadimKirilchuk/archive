package lol.guesshero.server

import lol.guesshero.server.Player

class Score {

    static constraints = {
    }

    static belongsTo = [player: Player]

    Date dateCreated
    Date dateUpdated
    
    long score
}
