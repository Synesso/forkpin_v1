package forkpin

import forkpin.Persistent.User
import RankAndFile._

class MoveEvaluator(game: Game, user: User) {

  def evaluate(from: RankAndFile, to: RankAndFile): Either[InvalidMove, Move] = {

    lazy val isUsersTurn = game.nextMove == user

    lazy val piece: Option[Piece] = game.board.pieceAt(from)

    lazy val validMoves: Map[RankAndFile, Move] = piece.map(_.validMoves(from, game.board)).getOrElse(Set.empty)
      .map{m => (m.to, m)}.toMap

    lazy val isOwnedPiece = piece.exists(_.colour == game.nextColour)

    lazy val canMoveToTarget = validMoves.contains(to)

//    lazy val isInCheckAfterMove = ??? // todo



    def invalid(reason: String): Either[InvalidMove, Move] = Left(InvalidMove(game, user, from, to, reason))

    if (!isUsersTurn) invalid(s"It is not the turn for user ${user.gPlusId}")
    else if (!isOwnedPiece) invalid(s"The piece $piece at $from is not owned by user ${user.gPlusId}")
    else if (!canMoveToTarget) invalid(s"The piece $piece at $from cannot move to $to")
    // ...
    else Right(validMoves(to))

  }

}
