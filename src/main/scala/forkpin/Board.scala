package forkpin

import forkpin.RankAndFile._

case class Board(pieces: Vector[Option[Piece]] = Vector.fill(64)(None)) {
  def pieceAt(rf: RankAndFile) = pieces(rf.id)
  def colourAt(rf: RankAndFile) = pieceAt(rf).map(_.colour)
}

abstract class BoardSide(val offsetRank: Int = 0, val offsetFile: Int = 0) {
  def +(side: BoardSide) = new BoardSide(
    offsetRank = BoardSide.this.offsetRank + side.offsetRank,
    offsetFile = BoardSide.this.offsetFile + side.offsetFile){}

  def adjacent: Set[BoardSide] = this match {
    case QueenSide | KingSide => Set(BlackSide, WhiteSide)
    case WhiteSide | BlackSide => Set(KingSide, QueenSide)
    case _ => Set.empty
  }
}

case object QueenSide extends BoardSide(offsetFile = -1)
case object KingSide extends BoardSide(offsetFile = 1)
case object BlackSide extends BoardSide(offsetRank = -1)
case object WhiteSide extends BoardSide(offsetRank = 1)
