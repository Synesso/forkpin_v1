package forkpin

import org.specs2.{ScalaCheck, Specification}
import org.scalacheck.{Gen, Arbitrary}
import org.scalacheck.Gen._
import org.specs2.mock.Mockito
import forkpin.persist.Persistent.User
import org.specs2.execute.Result


class SerialisationSpec extends Specification with ScalaCheck with Mockito { def is = s2"""

  A list of moves should
    have length being a multiple of 4 $lengthMultipleOf4
    every odd character must be a valid file $oddCharsAreFiles
    every even character must be a valid rank $evenCharsAreRanks

"""

  def lengthMultipleOf4 = pickledArbitraryMoves(_.length % 4 must beEqualTo(0))

  def oddCharsAreFiles = pickledArbitraryMoves(_.grouped(2).foldLeft("")
    {(acc,next) => acc + next.head}.forall{c => c must (beGreaterThanOrEqualTo('A') and beLessThanOrEqualTo('H'))})

  def evenCharsAreRanks = pickledArbitraryMoves(_.grouped(2).foldLeft("")
    {(acc,next) => acc + next.tail.head}.forall{c => c must (beGreaterThanOrEqualTo('1') and beLessThanOrEqualTo('8'))})

  val moves = (for {from <- RankAndFile.values; to <- RankAndFile.values} yield Move(from, to)).toSeq
  implicit val arbitraryMoveList = Arbitrary(listOf(Gen.oneOf(moves)))

  def pickledArbitraryMoves(p: (String) => Result) = prop{(moves: List[Move]) =>
    val game = Game(1, white, black).copy(moves = moves.toVector)
    p(game.pickledMoves)
  }

  val white, black = mock[User]
}
