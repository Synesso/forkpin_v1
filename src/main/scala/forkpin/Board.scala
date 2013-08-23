package forkpin

import forkpin.RankAndFile._

case class Board(pieces: Vector[Option[Piece]] = Vector.fill(64)(None)) {
  def pieceAt(rf: RankAndFile) = pieces(rf.id)
  def colourAt(rf: RankAndFile) = pieceAt(rf).map(_.colour)
}
