package forkpin

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import RankAndFile._
import forkpin.Persistent.GameRow

class SerialisationSpec extends FlatSpec with ShouldMatchers {

  val moves = Vector(Move(E2, E4), Move(E7, E5), Move(B1, C3))

  "a list of moves" should "serialise correctly" in {
    assert(Game(1, null, null, null, moves = moves).pickledMoves === "E2E4E7E5B1C3")
  }

}
