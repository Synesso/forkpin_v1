package forkpin

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import RankAndFile._
import forkpin.persist.Persistent
import Persistent.User

class ThreatSpec extends FlatSpec with ShouldMatchers {

  "any square" should "have no threat on an empty board" in {
    assert(game().isThreatenedAt(E5) === false)
  }

  it should "be threatened by a queen on the rank" in {
    assert(game(E2 -> BlackQueen).isThreatenedAt(E5))
  }

  it should "be threatened by a queen on the file" in {
    assert(game(H5 -> BlackQueen).isThreatenedAt(A5))
  }

  it should "be threatened by a queen on the diagonal" in {
    assert(game(H5 -> BlackQueen).isThreatenedAt(D1))
  }

  it should "not be threatened by a queen on the oblique" in {
    assert(!game(F7 -> BlackQueen).isThreatenedAt(B4))
  }

  it should "not be threatened by a friendly queen" in {
    assert(!game(F7 -> WhiteQueen).isThreatenedAt(F2))
  }

  it should "not be threatened by a queen when another enemy piece is in the way" in {
    assert(!game(F1 -> BlackQueen, F3 -> BlackKnight).isThreatenedAt(F5))
  }

  it should "not be threatened by a queen when another friendly piece is in the way" in {
    assert(!game(F1 -> BlackQueen, F3 -> WhitePawn).isThreatenedAt(F5))
  }

  it should "be threatened by a king in the next square along diagonal" in {
    assert(game(A8 -> BlackKing).isThreatenedAt(B7))
  }

  it should "be threatened by a king in the next square along rank" in {
    assert(game(A8 -> BlackKing).isThreatenedAt(A7))
  }

  it should "be threatened by a king in the next square along file" in {
    assert(game(A8 -> BlackKing).isThreatenedAt(B8))
  }

  it should "not be threatened by a king two squares along file" in {
    assert(!game(A8 -> BlackKing).isThreatenedAt(C8))
  }

  it should "not be threatened by a king two squares along rank" in {
    assert(!game(A8 -> BlackKing).isThreatenedAt(A6))
  }

  it should "not be threatened by a king two squares along diagonal" in {
    assert(!game(A8 -> BlackKing).isThreatenedAt(C6))
  }

  it should "not be threatened by a friendly king" in {
    assert(!game(D7 -> WhiteKing).isThreatenedAt(C6))
  }

  it should "be threatened by a rook on the rank" in {
    assert(game(A5 -> BlackRook).isThreatenedAt(A7))
  }

  it should "be threatened by a rook on the file" in {
    assert(game(A5 -> BlackRook).isThreatenedAt(H5))
  }

  it should "not be threatened by a rook on the diagonal" in {
    assert(!game(C4 -> BlackRook).isThreatenedAt(D5))
  }

  it should "not be threatened by a rook on the oblique" in {
    assert(!game(H2 -> BlackRook).isThreatenedAt(G4))
  }

  it should "not be threatened by a friendly rook" in {
    assert(!game(A5 -> WhiteRook).isThreatenedAt(H5))
  }

  it should "not be threatened by a rook obscured by enemy piece" in {
    assert(!game(A5 -> BlackRook, D5 -> BlackKnight).isThreatenedAt(H5))
  }

  it should "not be threatened by a rook obscured by friendly piece" in {
    assert(!game(A5 -> BlackRook, D5 -> WhiteRook).isThreatenedAt(H5))
  }


  val (white, black) = (User("w", null), User("b", null))

  def game(pieces: (RankAndFile, Piece)*): Game = {
    val b = Board(pieces = pieces.foldLeft(Vector.fill(64)(None: Option[Piece])){(arr, next) =>
      arr.updated(next._1.id, Some(next._2))
    })
    Game(1, white, black, white, board = b)
  }

}
