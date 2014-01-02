package forkpin

import forkpin.persist.User
import RankAndFile._

case class Move(from: RankAndFile, to: RankAndFile,
                capture: Option[RankAndFile] = None, // todo - this is not being used
                implication: Option[ImpliedMove] = None,
                promote: Option[Promotion] = None,
                enPassantTarget: Option[RankAndFile] = None) {
  lazy val forClient: Map[String, Any] = Map("from" -> from.toString.toLowerCase, "to" -> to.toString.toLowerCase)
}

case class Promotion(at: RankAndFile, to: Piece)

case class ImpliedMove(from: RankAndFile, to: RankAndFile)

case class InvalidMove(game: Game, user: User, from: RankAndFile, to: RankAndFile, reason: String) {
  lazy val forClient: Map[String, Any] = Map("reason" -> reason, "user" -> user.gPlusId, "game" -> game.forClient,
    "from" -> from, "to" -> to)
}

