package forkpin

import org.specs2.{ScalaCheck, Specification}
import org.scalacheck.{Gen, Arbitrary}
import org.scalacheck.Gen._
import org.specs2.mock.Mockito
import forkpin.persist.Persistent.User
import org.specs2.execute.Result


class SerialisationSpec extends Specification with ScalaCheck with Mockito { def is = s2"""

  A list of moves should
    have $lengthMultipleOf4


"""

  // todo - more work required

  def lengthMultipleOf4 = pickledArbitraryMoves(_.length % 4 must beEqualTo(0))

  val moves = (for {from <- RankAndFile.values; to <- RankAndFile.values} yield Move(from, to)).toSeq
  implicit val arbitraryMoveList = Arbitrary(listOf(Gen.oneOf(moves)))

  def pickledArbitraryMoves(p: (String) => Result) = prop{(moves: List[Move]) =>
    val game = Game(1, white, black).copy(moves = moves.toVector)
    p(game.pickledMoves)
  }

  val white, black = mock[User]
}
