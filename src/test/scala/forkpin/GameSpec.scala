package forkpin

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import forkpin.persist.Persistent
import Persistent.User

class GameSpec extends FlatSpec with ShouldMatchers {

  val black = User("black", null)
  val white = User("white", null)
  val aNewGame = Game(1, white, black)

  "A new game" should "have a starting position san" in {
    assert(aNewGame.san === "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR")
  }

  it should "have a starting position fen" in {
    assert(aNewGame.fen === "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
  }

  it should "expect white to play next" in {
    assert(aNewGame.nextMove === white)
  }

  it should "have no en passant target" in {
    aNewGame.enPassantTarget should be (None)
  }

  // todo - scalatest matchers are ... Move back to specs2
  it should "still permit all kinds of castling" in {
    aNewGame.castling.availabilityFor(White) should (
      contain (WhiteKingSide: CastlingAvailability) and
      contain (WhiteQueenSide: CastlingAvailability) and
      have size 2
    )
    aNewGame.castling.availabilityFor(Black) should (
      contain (BlackKingSide: CastlingAvailability) and
      contain (BlackQueenSide: CastlingAvailability) and
      have size 2
    )
  }

  it should "allow valid pawn moves" in {
//    aNewGame.move(white, E2, E4)
  }

  it should "disallow valid pawn moves" in {}

  it should "allow valid knight moves" in {}

  it should "disallow valid knight moves" in {}

  it should "disallow other pieces to move" in {}

}
