package forkpin

import org.specs2.{ScalaCheck, Specification}
import org.specs2.execute.Result

class SerialisationSpec extends Specification with ScalaCheck with TestImplicits { def is = s2"""

  A serialised list of moves should
    have length exactly 4 times the input $length4TimesInput
    every odd character must be a valid file $oddCharsAreFiles
    every even character must be a valid rank $evenCharsAreRanks

"""

  def length4TimesInput = prop{(moves: List[Move]) =>
    startGame.copy(moves = moves.toVector).pickledMoves.length must beEqualTo(moves.length * 4)
  }

  def oddCharsAreFiles = pickledArbitraryMoves(_.grouped(2).foldLeft("")
    {(acc,next) => acc + next.head}.forall{c => c must (beGreaterThanOrEqualTo('A') and beLessThanOrEqualTo('H'))})

  def evenCharsAreRanks = pickledArbitraryMoves(_.grouped(2).foldLeft("")
    {(acc,next) => acc + next.tail.head}.forall{c => c must (beGreaterThanOrEqualTo('1') and beLessThanOrEqualTo('8'))})

  def pickledArbitraryMoves(p: (String) => Result) = prop{(moves: List[Move]) =>
    p(startGame.copy(moves = moves.toVector).pickledMoves)
  }
}
