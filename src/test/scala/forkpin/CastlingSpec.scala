package forkpin

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec

class CastlingSpec extends FlatSpec with ShouldMatchers {

  "The flag" should "be a dash when no options are available" in {
    assertCastling("-")
  }

  it should "be 'k' when only black can move kingside" in {
    assertCastling("k", Black -> BlackKingSide) // todo - why do I specify colour twice?
  }

  it should "be 'K' when only white can move kingside" in {
    assertCastling("K", White -> WhiteKingSide)
  }

  it should "be 'q' when only black can move queenside" in {
    assertCastling("q", Black -> BlackQueenSide)
  }

  it should "be 'Q' when only white can move queenside" in {
    assertCastling("Q", White -> WhiteQueenSide)
  }

  it should "be 'Kk' when only moves are kingside" in {
    assertCastling("Kk", Black -> BlackKingSide, White -> WhiteKingSide)
  }

  it should "be 'Qq' when only moves are queenside" in {
    assertCastling("Qq", Black -> BlackQueenSide, White -> WhiteQueenSide)
  }

  "availability" should "not fail when empty" in {
    assert(Castling(permitted = Map.empty).availabilityFor(White) === Seq.empty)
  }

  it should "return only the availability for the given colour" in {
    assert(Castling().availabilityFor(Black) === Seq(BlackKingSide, BlackQueenSide))
  }

  def assertCastling(expected: String, entries: (Colour, CastlingAvailability)*) = {
    val map = entries.foldLeft(Map.empty[Colour, Seq[CastlingAvailability]]){case (map, (colour, ca)) =>
      map.updated(colour, ca +: map.getOrElse(colour, Seq.empty[CastlingAvailability]))
    }
    assert(Castling(map).flag === expected)
  }

  // todo - this is an interesting exercise for scalacheck - for non-empty, always caps before lower.
  // always K before Q within case, always white is caps, always black is lower
}
