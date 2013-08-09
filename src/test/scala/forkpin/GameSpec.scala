package forkpin

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import forkpin.Persistent.User

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

  it should "still permit all kinds of castling" in {
    aNewGame.castling.sides should (
      contain (BlackQueen: Piece) and
      contain (BlackKing: Piece) and
      contain (WhiteQueen: Piece) and
      contain (WhiteKing: Piece) and
      have size 4
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
