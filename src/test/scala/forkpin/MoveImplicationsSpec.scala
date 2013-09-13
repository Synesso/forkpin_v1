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

  private def have(piece: Piece) = new {
    def at(rf: RankAndFile) = {
      val pf: PartialFunction[Game, MatchResult[_]] = {
        case (g: Game) => g.board.pieceAt(rf) must beSome(piece)
      }
      beRight.like(pf)
    }
  }


}
