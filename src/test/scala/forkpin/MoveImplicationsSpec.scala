package forkpin

import org.specs2.Specification
import RankAndFile._
import org.specs2.matcher.MatchResult

class MoveImplicationsSpec extends Specification with TestImplicits { def is = s2"""

  The rook should also move when
    the white king castles king side $rookWhenWhiteKingKingSide
    the white king castles queen side $rookWhenWhiteKingQueenSide
    the black king castles king side $rookWhenBlackKingKingSide
    the black king castles queen side $rookWhenBlackKingQueenSide

  The en passant flag should be set when
    the pawn has moved two spaces on first move $enPassantFlag

  The enemy pawn should be removed when
    the white pawn performs en passant $pawnWhenWhiteEnPassant
    the black pawn performs en passant $pawnWhenBlackEnPassant

  The move history should
    not include the castling implication $noRookInMoveHistory

"""

  def rookWhenWhiteKingKingSide = {
    castleGame.move(white, from = E1, to = G1) must have(WhiteRook).at(F1)
  }

  def rookWhenWhiteKingQueenSide = {
    castleGame.move(white, from = E1, to = C1) must have(WhiteRook).at(D1)
  }

  def rookWhenBlackKingKingSide = {
    castleGame.copy(nextMove = black).move(black, from = E8, to = G8) must have(BlackRook).at(F8)
  }

  def rookWhenBlackKingQueenSide = {
    castleGame.copy(nextMove = black).move(black, from = E8, to = C8) must have(BlackRook).at(D8)
  }

  def enPassantFlag = {
    startGame.move(white, C2, C4) must beRight.like{case (g: Game) => g.enPassantTarget must beSome(C3)}
  }

  def pawnWhenWhiteEnPassant = {
    val epGame = game(B7 -> BlackPawn, C5 -> WhitePawn).copy(nextMove = black).move(black, B7, B5).right.get
    epGame.move(white, C5, B6) must beRight.like{case (g: Game) => g.isOccupiedAt(B5) must beFalse}
  }

  def pawnWhenBlackEnPassant = {
    val epGame = game(E2 -> WhitePawn, D4 -> BlackPawn).move(white, E2, E4).right.get
    epGame.move(black, D4, E3) must beRight.like{case (g: Game) => g.isOccupiedAt(E4) must beFalse}
  }

  def noRookInMoveHistory = {
    castleGame.move(white, from = E1, to = C1) must beRight.like{case (g: Game) =>
      g.moves must not contain{(m: Move) => (m.from, m.to) === (A1, D1) }
    }
  }

  private def have(piece: Piece) = new {
    def at(rf: RankAndFile) = {
      val pf: PartialFunction[Game, MatchResult[_]] = {
        case (g: Game) => g.board.pieceAt(rf) must beSome(piece)
      }
      beRight.like(pf)
    }
  }


}
