package forkpin

import forkpin.persist.Persistent.User
import RankAndFile._
import org.specs2.Specification

class MovesSpec extends Specification { def is = s2"""

  A rook should
    move along its own rank and file only $e1
    not be able to move to its from square $e2
  """
  val game = Game(1, User("w", null), User("b", null))

  def e1 = WhiteRook.validMoves(D6, game).map(_.to) must containTheSameElementsAs(
    Seq(D1, D2, D3, D4, D5, D7, D8, A6, B6, C6, E6, F6, G6, H6))

  def e2 = WhiteRook.validMoves(D6, game).map(_.to) must not contain D6
}
