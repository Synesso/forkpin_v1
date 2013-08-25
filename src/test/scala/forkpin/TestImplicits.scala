package forkpin

import forkpin.RankAndFile._
import scala.Some

object TestImplicits {

  implicit class GameWrapper(game: Game) {
    def place(rfPiece: (RankAndFile, Piece)) = {
      val (rf, piece) = rfPiece
      val newPieces = game.board.pieces.updated(rf.id, Some(piece))
      game.copy(board = game.board.copy(pieces = newPieces))
    }

    def surround(rf: RankAndFile, piece: Piece) = {
      val surroundingSquares = (for (r <- Seq(-1, 0, 1); f <- Seq(-1, 0, 1)) yield (rf.r + r, rf.f + f)).filter{ case (r, f) =>
        r >= 0 && r <= 7 && f >= 0 && f <= 7 && (r != rf.r || f != rf.f)
      }.map{case (r, f) => RankAndFile(r * 8 + f)}
      surroundingSquares.foldLeft(game){case (buildingGame, square) => buildingGame.place(square, piece)}
    }

    def printBoard(): Unit = {
      game.board.pieces.zipWithIndex.foreach{case (op, i) =>
        print(op.map(p => p.sanModifier(p.sanRole)).getOrElse("."))
        if (i % 8 == 7) println()
      }
    }
  }

}
