import forkpin.RankAndFile._
import scala.Some

package object forkpin {

  val roles = Seq(King, Queen, Rook, Bishop, Knight, Pawn)

  implicit class GameWrapper(game: Game) {
    def place(rfPiece: (RankAndFile, Piece)) = {
      val (rf, piece) = rfPiece
      val newPieces = game.board.pieces.updated(rf.id, Some(piece))
      game.copy(board = game.board.copy(pieces = newPieces))
    }
  }

}
