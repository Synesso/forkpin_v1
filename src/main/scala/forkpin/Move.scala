package forkpin

import forkpin.persist.Persistent
import Persistent.User
import RankAndFile._

case class Move(from: RankAndFile, to: RankAndFile,
                capture: Option[RankAndFile] = None,
                implication: Option[Move] = None,
                promote: Option[Promotion] = None) {
  lazy val forClient: Map[String, Any] = Map("from" -> from.toString, "to" -> to.toString)
}

case class Promotion(at: RankAndFile, to: Piece)

case class InvalidMove(game: Game, user: User, from: RankAndFile, to: RankAndFile, reason: String) {
  lazy val forClient: Map[String, Any] = Map("reason" -> reason, "user" -> user.gPlusId, "game" -> game.forClient,
    "from" -> from, "to" -> to)
}

