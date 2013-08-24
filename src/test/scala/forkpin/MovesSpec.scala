package forkpin

import forkpin.persist.Persistent.User
import RankAndFile._
import org.specs2.Specification

class MovesSpec extends Specification { def is = s2"""

  A rook should
    move along its own rank and file only $rook1
    include and stop at enemy pieces & exclude and stop at friendly pieces $rook2

  A knight should
    move in a dogleg $knight1
    include enemy pieces $knight2
    exclude friendly pieces $knight3

  A bishop should
    move diagonally $bishop1
    include and stop at enemy pieces & exclude and stop at friendly pieces $bishop2

  A queen should
    move along rank, file and diagonally $queen1
    include and stop at enemy pieces & exclude and stop at friendly pieces $queen2

  A king should
    move along rank, file and diagonally to a depth of 1 $king1
    include and stop at enemy pieces $king2
    exclude and stop at friendly pieces $king3
    castle kingside when permitted $king4
    not castle kingside when denied $king5
    castle queenside when permitted $king6
    not castle queenside when denied $king7

  """

  val emptyGame = Game(1, User("w", null), User("b", null), User("w", null), board = Board())
  val startGame = Game(1, User("w", null), User("b", null), User("w", null))

  def rook1 = WhiteRook.validMoves(D6, emptyGame).map(_.to) must containTheSameElementsAs(
    Seq(D1, D2, D3, D4, D5, D7, D8, A6, B6, C6, E6, F6, G6, H6))

  def rook2 = BlackRook.validMoves(E4, startGame).map(_.to) must containTheSameElementsAs(
    Seq(E2, E3, E5, E6, A4, B4, C4, D4, F4, G4, H4)
  )

  def knight1 = WhiteKnight.validMoves(D5, emptyGame).map(_.to) must containTheSameElementsAs(
    Seq(B4, B6, C3, C7, E3, E7, F4, F6)
  )

  def knight2 = BlackKnight.validMoves(C3, startGame).map(_.to) must containTheSameElementsAs(
    Seq(A2, A4, B1, B5, D1, D5, E2, E4)
  )

  def knight3 = BlackKnight.validMoves(B8, startGame).map(_.to) must containTheSameElementsAs(
    Seq(A6, C6)
  )

  def bishop1 = WhiteBishop.validMoves(F6, emptyGame).map(_.to) must containTheSameElementsAs(
    Seq(E5, E7, D4, D8, C3, B2, A1, G5, G7, H4, H8)
  )

  def bishop2 = WhiteBishop.validMoves(F6, startGame).map(_.to) must containTheSameElementsAs(
    Seq(E5, E7, D4, C3, G5, G7, H4)
  )

  def queen1 = BlackQueen.validMoves(F2, emptyGame).map(_.to) must containTheSameElementsAs(
    Seq(F1, F3, F4, F5, F6, F7, F8, A2, B2, C2, D2, E2, G2, H2, E1, E3, D4, C5, B6, A7, G1, G3, H4)
  )

  def queen2 = BlackQueen.validMoves(F2, startGame).map(_.to) must containTheSameElementsAs(
    Seq(E1, F1, G1, E2, G2, E3, D4, C5, B6, G3, H4, F3, F4, F5, F6)
  )

  def king1 = WhiteKing.validMoves(B4, emptyGame).map(_.to) must containTheSameElementsAs(
    Seq(A3, B3, C3, C4, C5, B5, A5, A4)
  )

  def king2 = WhiteKing.validMoves(B6, startGame).map(_.to) must containTheSameElementsAs(
    Seq(A7, B7, C7, C6, C5, B5, A5, A6)
  )

  def king3 = WhiteKing.validMoves(B3, startGame).map(_.to) must containTheSameElementsAs(
    Seq(A3, A4, B4, C4, C3)
  )

  // todo - from here

  def king4 = pending
  def king5 = pending
  def king6 = pending
  def king7 = pending
}
