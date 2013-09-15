package forkpin

import forkpin.persist.Persistent
import Persistent.User
import org.specs2.{ScalaCheck, Specification}
import RankAndFile._
import org.scalacheck.{Arbitrary, Gen}

class GameSpec extends Specification with ScalaCheck { def is = s2"""

  A new game must
    have a starting position san $startPositionSan
    have a starting position fen $startPositionFen
    expect white to play $expectWhiteToPlay
    have no en passant target $haveNoEnPassantTarget
    permit any kind of castling $allowAllCastling
    allow white pawns to move 1 or 2 squares $allWhitePawnsToMove1Or2Squares
    allow white knights to move to 2 valid squares $whiteKnightsCanMove
    disallow any other move $nothingElseCanMove

"""

  val black = User("black", null)
  val white = User("white", null)
  val aNewGame = Game(1, white, black)

  def startPositionSan = aNewGame.san must_== "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"

  def startPositionFen = aNewGame.fen must_== "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

  def expectWhiteToPlay = aNewGame.nextMove must_== white

  def haveNoEnPassantTarget = aNewGame.enPassantTarget must beNone

  def allowAllCastling = aNewGame.castling.permitted must haveTheSameElementsAs(
    Set(BlackKingSide, BlackQueenSide, WhiteKingSide, WhiteQueenSide))

  def allWhitePawnsToMove1Or2Squares = Arbitrary(pawnMoves){case (from: RankAndFile, to: RankAndFile) =>
    aNewGame.move(from, to) must beRight
  }

  def whiteKnightsCanMove = Arbitrary(knightMoves){case (from: RankAndFile, to: RankAndFile) =>
    aNewGame.move(from, to) must beRight
  }

  def nothingElseCanMove = Arbitrary(invalidMoves) {case (from: RankAndFile, to: RankAndFile) =>
    aNewGame.move(from, to) must beLeft
  }.set(minTestsOk = 4000)

  val pawnMoves = for {
    from <- Gen.oneOf(A2 to H2)
    to <- Gen.oneOf(Seq(8, 16).flatMap(from.-))
  } yield (from, to)

  val knightMoves = for {
    from <- Gen.oneOf(B1, G1)
    to <- Gen.oneOf(Seq(15, 17).flatMap(from.-))
  } yield (from, to)

  val invalidMoves = {
    def validPawnMove(from: RankAndFile, to: RankAndFile) =
      from >= A2 && from <= H2 && Seq(8, 16).flatMap(from.-).contains(to)
    def validKnightMove(from: RankAndFile, to: RankAndFile) =
      (from == B1 || from == G1) && Seq(15, 17).flatMap(from.-).contains(to)
    (for {
      from <- Gen.oneOf(RankAndFile.values.toSeq)
      to <- Gen.oneOf(RankAndFile.values.toSeq)
    } yield (from, to)) suchThat { case (from, to) => !(validPawnMove(from, to) || validKnightMove(from, to)) }
  }

}
