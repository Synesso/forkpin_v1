package forkpin

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import RankAndFile._
import forkpin.persist.Persistent
import Persistent.User
import org.specs2.{ScalaCheck, Specification}
import org.scalacheck.{Gen, Arbitrary}
import org.specs2.execute.Result
import org.specs2.mock.Mockito

class ThreatSpec extends Specification with ScalaCheck with TestImplicits with Mockito { def is = s2"""

  Any square should
    have $noThreatOnEmptyBoard
    be threatened by $queenOnRankFileOrDiagonal
    not be threatened by $friendlyQueen
    not be threatened by $blockedQueen

"""

  def noThreatOnEmptyBoard = "no threat on an empty board" ! prop {(square: RankAndFile) =>
    emptyGame.isThreatenedAt(square) must beFalse
  }

  def queenOnRankFileOrDiagonal = "a queen on the rank, file or diagonal" ! prop {
    (square: RankAndFile, enemySquare: RankAndFile) =>
    val threat = square != enemySquare && square.onSameQueenMovementAs(enemySquare)
    game(enemySquare -> BlackQueen).isThreatenedAt(square) must beEqualTo(threat)
  }

  def friendlyQueen = "a friendly queen at any square" ! prop{(square: RankAndFile, friendlySquare: RankAndFile) =>
    game(friendlySquare -> WhiteQueen).isThreatenedAt(square) must beFalse
  }

  def blockedQueen = "a blocked enemy queen" ! prop{(squaresTriple: (RankAndFile, RankAndFile, RankAndFile)) =>
    val squares = Seq(squaresTriple._1, squaresTriple._2, squaresTriple._3).sorted
    val blockedQueenGame = game().place(squares(1), BlackKnight).place(squares(2), BlackQueen)
    blockedQueenGame.isThreatenedAt(squares(0)) must beFalse
  }

  val squares: Gen[RankAndFile.Value] = Gen.oneOf(RankAndFile.values.toSeq)

  val pieces: Gen[Piece] = Gen.oneOf(for {
    role <- Seq(Pawn, Knight, Bishop, Rook, Queen, King)
    colour <- Seq(Black, White)
  } yield colour sided role)

  implicit val arbitrarySquare = Arbitrary(squares)
  implicit val arbitraryPiece = Arbitrary(pieces)

  implicit val arbitrarySquareTriple = Arbitrary(for {
    square1 <- squares
    square2 <- Gen.oneOf((RankAndFile.lines(square1.onSameQueenMovementAs) - square1).toSeq)
    square3 <- Gen.oneOf((square1.lineOf(square2) - square1 - square2).toSeq)
  } yield (square1, square2, square3))


  /*

    it should "not be threatened by a queen when another enemy piece is in the way" in {
      assert(!game(F1 -> BlackQueen, F3 -> BlackKnight).isThreatenedAt(F5))
    }

    it should "not be threatened by a queen when another friendly piece is in the way" in {
      assert(!game(F1 -> BlackQueen, F3 -> WhitePawn).isThreatenedAt(F5))
    }

    it should "be threatened by a king in the next square along diagonal" in {
      assert(game(A8 -> BlackKing).isThreatenedAt(B7))
    }

    it should "be threatened by a king in the next square along rank" in {
      assert(game(A8 -> BlackKing).isThreatenedAt(A7))
    }

    it should "be threatened by a king in the next square along file" in {
      assert(game(A8 -> BlackKing).isThreatenedAt(B8))
    }

    it should "not be threatened by a king two squares along file" in {
      assert(!game(A8 -> BlackKing).isThreatenedAt(C8))
    }

    it should "not be threatened by a king two squares along rank" in {
      assert(!game(A8 -> BlackKing).isThreatenedAt(A6))
    }

    it should "not be threatened by a king two squares along diagonal" in {
      assert(!game(A8 -> BlackKing).isThreatenedAt(C6))
    }

    it should "not be threatened by a friendly king" in {
      assert(!game(D7 -> WhiteKing).isThreatenedAt(C6))
    }

    it should "be threatened by a rook on the rank" in {
      assert(game(A5 -> BlackRook).isThreatenedAt(A7))
    }

    it should "be threatened by a rook on the file" in {
      assert(game(A5 -> BlackRook).isThreatenedAt(H5))
    }

    it should "not be threatened by a rook on the diagonal" in {
      assert(!game(C4 -> BlackRook).isThreatenedAt(D5))
    }

    it should "not be threatened by a rook on the oblique" in {
      assert(!game(H2 -> BlackRook).isThreatenedAt(G4))
    }

    it should "not be threatened by a friendly rook" in {
      assert(!game(A5 -> WhiteRook).isThreatenedAt(H5))
    }

    it should "not be threatened by a rook obscured by enemy piece" in {
      assert(!game(A5 -> BlackRook, D5 -> BlackKnight).isThreatenedAt(H5))
    }

    it should "not be threatened by a rook obscured by friendly piece" in {
      assert(!game(A5 -> BlackRook, D5 -> WhiteRook).isThreatenedAt(H5))
    }
  */


  val (white, black) = (mock[User], mock[User])

  def game(pieces: (RankAndFile, Piece)*): Game = {
    val b = Board(pieces = pieces.foldLeft(Vector.fill(64)(None: Option[Piece])){(arr, next) =>
      arr.updated(next._1.id, Some(next._2))
    })
    Game(1, white, black, white, board = b)
  }

  val emptyGame = game()

}
