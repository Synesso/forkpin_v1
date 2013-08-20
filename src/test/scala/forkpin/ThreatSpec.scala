package forkpin

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import RankAndFile._
import forkpin.Persistent.User

class ThreatSpec extends FlatSpec with ShouldMatchers {

  "any square" should "have no threat on an empty board" in {
    assert(game().isThreatenedAt(E5) === false)
  }

  it should "be threatened by a queen on the rank" in {
    assert(game(E2 -> BlackQueen).isThreatenedAt(E5))
  }

  val (white, black) = (User("w", null), User("b", null))

  def game(pieces: (RankAndFile, Piece)*): Game = {
    val b = Board(pieces = pieces.foldLeft(Vector.fill(64)(None: Option[Piece])){(arr, next) =>
      arr.updated(next._1.id, Some(next._2))
    })
    Game(1, white, black, white, board = b)
  }

}
