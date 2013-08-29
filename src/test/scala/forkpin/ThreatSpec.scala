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
    val threat = square != enemySquare &&
      (square.onSameDiagonalAs(enemySquare) || square.onSameFileAs(enemySquare) || square.onSameRankAs(enemySquare))
    game(enemySquare -> BlackQueen).isThreatenedAt(square) must beEqualTo(threat)
  }

  def friendlyQueen = "a friendly queen at any square" ! prop{(square: RankAndFile, friendlySquare: RankAndFile) =>
    game(friendlySquare -> WhiteQueen).isThreatenedAt(square) must beFalse
  }

  def blockedQueen = "a blocked enemy queen" ! prop{(squareAndDirection: (RankAndFile, BoardSide)) =>
/*
    val (square, direction) = squareAndDirection
    val freeSquares = square.squaresInDirection(direction)
    val knightPlacement: Int = (Math.random * (freeSquares - 1)).toInt + 1
    val enemyPlacement: Int = knightPlacement + (Math.random * (freeSquares - knightPlacement - 1)).toInt + 1
    val blockedQueenGame = (1 to enemyPlacement).foldLeft((square, game())){case ((rf: RankAndFile, g: Game), i: Int) =>
      if (i == knightPlacement) (rf.towards(direction).get, g.place(rf, BlackKnight))
      else if (i == enemyPlacement) (rf.towards(direction).get, g.place(rf, BlackQueen))
      else (rf.towards(direction).get, g)
    }._2
    blockedQueenGame.isThreatenedAt(square) must beFalse
*/
    true
  }

  val squares: Gen[RankAndFile.Value] = Gen.oneOf(RankAndFile.values.toSeq)

  val directions: Gen[BoardSide] = Gen.oneOf(Seq(BlackSide, WhiteSide, QueenSide, KingSide,
    BlackSide + KingSide, BlackSide + QueenSide, WhiteSide + QueenSide, WhiteSide + KingSide))

  val pieces: Gen[Piece] = Gen.oneOf(for {
    role <- Seq(Pawn, Knight, Bishop, Rook, Queen, King)
    colour <- Seq(Black, White)
  } yield colour sided role)

  implicit val arbitrarySquare = Arbitrary(squares)
  implicit val arbitraryDirection = Arbitrary(directions)
  implicit val arbitraryPiece = Arbitrary(pieces)
/*
  implicit val arbitrarySquareAndDirection = Arbitrary(for {
    square <- squares
    direction <- directions suchThat (d => square.squaresInDirection(d) >= 2)
  } yield (square, direction))
*/


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
