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

    lazy val (r, f) = (rf.id / 8, rf.id % 8)

    def towards(direction: BoardSide): Option[RankAndFile] = {
      val (newR, newF) = (r + direction.offsetRank, f + direction.offsetFile)
      if (newR > 7 || newF > 7 || newR < 0 || newF < 0) None
      else {
        val id = rf.id + (direction.offsetRank * 8) + direction.offsetFile
        Some(RankAndFile(id))
      }
    }

    def seek(game: Game, colour: Colour, directions: BoardSide*): Set[Move] =
      seekPositions(game, colour, 8, allowCapture = true, directions)

    def seek(game: Game, colour: Colour, depth: Int, directions: BoardSide*): Set[Move] =
      seekPositions(game, colour, depth, allowCapture = true, directions)

    def seek(game: Game, colour: Colour, depth: Int, allowCapture: Boolean, directions: BoardSide*): Set[Move] =
      seekPositions(game, colour, depth, allowCapture, directions)

    private def seekPositions(game: Game, colour: Colour, depth: Int, allowCapture: Boolean, directions: Seq[BoardSide]): Set[Move] = {
      val enemy = colour.opposite
      def seek(found: Set[Move], last: RankAndFile, remainingDepth: Int, remainingDirections: Seq[BoardSide]): Set[Move] = {
        remainingDirections match {
          case direction +: tail => {
            if (remainingDepth == 0) seek(found, rf, depth, tail)
            else last.towards(direction).map{nextRf =>
              game.board.colourAt(nextRf).map{colourHere =>
                if (allowCapture && colourHere.colour == enemy) {
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

    def +(i: Int) = if (rf.id + i < 0 || rf.id + i >= 64) None else Some(RankAndFile(rf.id + i))
    def -(i: Int) = this.+(i * -1)

    def to(that: RankAndFile): Seq[RankAndFile] = (rf.id to that.id).map(RankAndFile.apply)

    def onSameFileAs(that: RankAndFile) = f == that.f
    def onSameRankAs(that: RankAndFile) = r == that.r
    def onSameDiagonalAs(that: RankAndFile) = math.abs(r - that.r) == math.abs(f - that.f)
    def squaresInDirection(direction: BoardSide): Int = {
      def inner(rf: RankAndFile, count: Int): Int = {
        (this towards direction).map(next => inner(next, count + 1)).getOrElse(count)
      }
      inner(rf, 0)
    }
  }
}
