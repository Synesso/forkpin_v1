package forkpin

import RankAndFile._
import forkpin.persist.Persistent
import Persistent.User
import org.specs2.{ScalaCheck, Specification}
import org.scalacheck.{Gen, Arbitrary}
import org.specs2.mock.Mockito

class ThreatSpec extends Specification with ScalaCheck with TestImplicits with Mockito { def is = s2"""

  Any square should
    have $noThreatOnEmptyBoard

  A queen should
    threaten on the rank, file or diagonal $queenOnRankFileOrDiagonal
    not threatened when blocked $blockedQueen

  A king should
    threated on the adjacent square $kingOnTheNextSquare
    not threaten on a non-adjacent square $kingOnANonAdjacentSquare

  A friendly piece should
    never threaten $friendlyPieces


"""

  def noThreatOnEmptyBoard = "no threat on an empty board" ! prop {(square: RankAndFile) =>
    emptyGame.isThreatenedAt(square) must beFalse
  }

  def queenOnRankFileOrDiagonal = prop {
    (square: RankAndFile, enemySquare: RankAndFile) =>
    val threat = square != enemySquare && square.onSameQueenMovementAs(enemySquare)
    game(enemySquare -> BlackQueen).isThreatenedAt(square) must beEqualTo(threat)
  }

  def friendlyPieces = prop{
    (square: RankAndFile, friendlySquare: RankAndFile, role: RoleMarker) =>
      game(friendlySquare -> White.sided(role)).isThreatenedAt(square) must beFalse
  }

  def blockedQueen = prop{(squaresTriple: (RankAndFile, RankAndFile, RankAndFile)) =>
    val squares = Seq(squaresTriple._1, squaresTriple._2, squaresTriple._3).sorted
    val blockedQueenGame = game().place(squares(1), BlackKnight).place(squares(2), BlackQueen)
    blockedQueenGame.isThreatenedAt(squares(0)) must beFalse
  }

  def kingOnTheNextSquare = arbitraryAdjacentSquares{
    case (first: RankAndFile, second: RankAndFile) =>
      game(first -> BlackKing).isThreatenedAt(second) must beTrue
  }

  def kingOnANonAdjacentSquare = arbitraryNonAdjacentSquares{
    case (first: RankAndFile, second: RankAndFile) =>
      game(first -> BlackKing).isThreatenedAt(second) must beFalse
  }

  val squares = Gen.oneOf(RankAndFile.values.toSeq)

  val roles = Gen.oneOf(Seq(Pawn, Knight, Bishop, Rook, Queen, King))

  val pieces = for {
    role <- roles
    colour <- Gen.oneOf(Seq(Black, White))
  } yield colour sided role

  implicit val arbitrarySquare = Arbitrary(squares)
  implicit val arbitraryRole = Arbitrary(roles)
  implicit val arbitraryPiece = Arbitrary(pieces)

  implicit val arbitrarySquareTriple = Arbitrary(for {
    square1 <- squares
    square2 <- Gen.oneOf((RankAndFile.lines(square1.onSameQueenMovementAs) - square1).toSeq)
    square3 <- Gen.oneOf((square1.lineOf(square2) - square1 - square2).toSeq)
  } yield (square1, square2, square3))

  val arbitraryAdjacentSquares = Arbitrary(for {
    first <- squares
    second <- Gen.oneOf(first.surroundingSquares)
  } yield (first, second))

  val arbitraryNonAdjacentSquares = Arbitrary(for {
    first <- squares
    second <- Gen.oneOf((RankAndFile.values -- first.surroundingSquares - first).toSeq)
  } yield (first, second))

  /*

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
