package forkpin

import forkpin.RankAndFile._
import scala.Some

trait TestImplicits {

  implicit class GameWrapper(game: Game) {
    def place(rfPiece: (RankAndFile, Piece)) = {
      val (rf, piece) = rfPiece
      val newPieces = game.board.pieces.updated(rf.id, Some(piece))
      game.copy(board = game.board.copy(pieces = newPieces))
    }

    def surround(rf: RankAndFile, piece: Piece) = {
      rf.surroundingSquares.foldLeft(game){case (buildingGame, square) => buildingGame.place(square, piece)}
    }

    def printBoard(): Unit = {
      game.board.pieces.zipWithIndex.foreach{case (op, i) =>
        print(op.map(p => p.sanModifier(p.sanRole)).getOrElse("."))
        if (i % 8 == 7) println()
      }
    }
  }

}
