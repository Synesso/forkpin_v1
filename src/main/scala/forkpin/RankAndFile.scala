package forkpin

object RankAndFile extends Enumeration {
  type RankAndFile = Value
  val A8, B8, C8, D8, E8, F8, G8, H8 = Value
  val A7, B7, C7, D7, E7, F7, G7, H7 = Value
  val A6, B6, C6, D6, E6, F6, G6, H6 = Value
  val A5, B5, C5, D5, E5, F5, G5, H5 = Value
  val A4, B4, C4, D4, E4, F4, G4, H4 = Value
  val A3, B3, C3, D3, E3, F3, G3, H3 = Value
  val A2, B2, C2, D2, E2, F2, G2, H2 = Value
  val A1, B1, C1, D1, E1, F1, G1, H1 = Value

  object +: {
    def unapply[T](s: Seq[T]) = s.headOption.map(head => (head, s.tail))
  }

  implicit class RankAndFileWrapper(rf: RankAndFile) {

    def towards(direction: BoardSide): Option[RankAndFile] = {
      val (x, y) = (rf.id / 8 + direction.offsetRank, rf.id % 8 + direction.offsetFile)
      if (x > 7 || y > 7 || x < 0 || y < 0) None
      else {
        val id = rf.id + (direction.offsetRank * 8) + direction.offsetFile
        Some(RankAndFile(id))
      }
    }

    def seek(game: Game, colour: Colour, directions: BoardSide*): Set[Move] = seekPositions(game, colour, 8, directions)

    def seek(game: Game, colour: Colour, depth: Int, directions: BoardSide*): Set[Move] = seekPositions(game, colour, depth, directions)

    private def seekPositions(game: Game, colour: Colour, depth: Int, directions: Seq[BoardSide]): Set[Move] = {
      val enemy = colour.opposite
      def seek(found: Set[Move], last: RankAndFile, remainingDepth: Int, remainingDirections: Seq[BoardSide]): Set[Move] = {
        remainingDirections match {
          case direction +: tail => {
            if (remainingDepth == 0) seek(found, rf, depth, tail)
            else last.towards(direction).map{nextRf =>
              game.board.colourAt(nextRf).map{colourHere =>
                if (colourHere.colour == enemy) {
                  seek(found + Move(rf, nextRf, Some(nextRf)), rf, depth, tail)
                } else {
                  seek(found, rf, depth, tail)
                }
              }.getOrElse{
                seek(found + Move(rf, nextRf), nextRf, remainingDepth - 1, remainingDirections)
              }
            }.getOrElse(seek(found, rf, depth, tail))
          }
          case Nil => found
        }
      }

      seek(Set.empty[Move], rf, depth, directions)
    }

  }
}
