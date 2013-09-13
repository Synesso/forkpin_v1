package forkpin

import forkpin.RankAndFile._
import scala.Some
import forkpin.persist.Persistent.User
import org.specs2.mock.Mockito

trait TestImplicits extends Mockito {

  val black = mock[User]
  val white = mock[User]
  val startGame = Game(1, white, black, white)
  val emptyGame = startGame.copy(board = Board())
  val castleGame = startGame.copy(board = Board(pieces = Vector[(RankAndFile, Piece)](
    A1 -> WhiteRook, E1 -> WhiteKing, H1 -> WhiteRook,
    A8 -> BlackRook, E8 -> BlackKing, H8 -> BlackRook
  ).foldLeft(Vector.fill(64)(None: Option[Piece])){(arr, next) =>
    arr.updated(next._1.id, Some(next._2))
  }))

  def game(pieces: (RankAndFile, Piece)*): Game = {
    val b = Board(pieces = pieces.foldLeft(Vector.fill(64)(None: Option[Piece])){(arr, next) =>
      arr.updated(next._1.id, Some(next._2))
    })
    Game(1, white, black, white, board = b)
  }

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
