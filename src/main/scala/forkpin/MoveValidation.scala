package forkpin

import forkpin.Persistent.User

class MoveValidation(game: Game, user: User, move: Move) {

  def validationError: Option[String] = {
    if (!isUsersTurn) Some(s"It is not the turn for user ${user.gPlusId}")
    else if (!isOwnedPiece) Some(s"The piece ${game.board.pieceAt(move.from)} at ${move.from} is not owned by user ${user.gPlusId}")
    else if (!canMoveToTarget) Some(s"The piece ${game.board.pieceAt(move.from)} at ${move.from} cannot move to ${move.to}")
    else None
  }

  def isUsersTurn = game.nextMove == user

  def isOwnedPiece = game.board.pieces(move.from.id).exists(_.colour == game.nextColour)

  def canMoveToTarget = game.board.pieces(move.from.id).exists(_.validMoves(move.from, game.board).contains(move.to))

  def isNotInCheckAfterMove = ???

  // todo - etc


}
