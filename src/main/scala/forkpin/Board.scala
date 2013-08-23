package forkpin

import forkpin.RankAndFile._

case class Board(pieces: Vector[Option[Piece]] = Vector.fill(64)(None)) {
  def pieceAt(rf: RankAndFile) = pieces(rf.id)
  def colourAt(rf: RankAndFile) = pieceAt(rf).map(_.colour)
}

trait BoardSide {
  val offset: Int
  def +(side: BoardSide) = new Object with BoardSide {
    val offset: Int = BoardSide.this.offset + side.offset
  }
}
case object QueenSide extends BoardSide {
  val offset = -1
}
case object KingSide extends BoardSide {
  val offset = 1
}
case object BlackSide extends BoardSide {
  val offset = -8
}
case object WhiteSide extends BoardSide {
  val offset = 8
}
