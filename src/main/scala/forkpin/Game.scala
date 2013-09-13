package forkpin

import forkpin.persist.Persistent
import Persistent._
import RankAndFile._

case class Game(id: Int, white: User, black: User,
                nextMove: User,
                board: Board = Board(pieces = Game.openingPosition),
                castling: Castling = Castling(),
                enPassantTarget: Option[RankAndFile] = None,
                halfMoveClock: Int = 0,
                moves: Vector[Move] = Vector.empty[Move]) {

  lazy val san = board.pieces.grouped(8).map{array =>
    val (row, nones) = array.foldLeft(("", 0)) {case ((str, noneCount), maybePiece) =>
      maybePiece.map{
        piece => (s"$str${nonZero(noneCount)}${piece.sanModifier(piece.sanRole)}", 0)
      }.getOrElse{
        (str, noneCount + 1)
      }
    }
    s"$row${nonZero(nones)}"
  }.mkString("/")
  private def nonZero(i: Int) = if (i == 0) "" else i.toString

  lazy val nextColour = if (nextMove equals white) White else Black

  lazy val enemy = if (nextMove equals white) black else white

  lazy val activeColour = nextColour.sanColour

  lazy val enPassantTargetFlag = enPassantTarget.map(_.toString.toLowerCase).getOrElse("-")

  lazy val fullMove = (moves.length / 2) + 1

  lazy val fen = s"$san $activeColour ${castling.flag} $enPassantTargetFlag $halfMoveClock $fullMove"

  lazy val pickledMoves = moves.map{m => m.from.toString + m.to.toString}.mkString

  // todo - it's kooky to ask who the user is. Bake white or black into the type?
  def move(user: User, from: String, to: String): Either[InvalidMove, Game] =
    move(user, RankAndFile.withName(from), RankAndFile.withName(to))

  def move(user: User, from: RankAndFile, to: RankAndFile): Either[InvalidMove, Game] = {
    new MoveEvaluator(this, user).evaluate(from, to).fold(
      (invalidMove) => Left(invalidMove),
      (move) => Right(applyMove(move))
    )
  }

  def isOccupiedAt(rf: RankAndFile) = board.pieceAt(rf).isDefined

  def isThreatenedAt(rf: RankAndFile) = {
    roles.exists{role =>
      val pretence = nextColour sided role
      val lookingFor = nextColour.opposite sided role
      pretence.validCapturingMoves(rf, this).exists{move => board.pieceAt(move.to) == Some(lookingFor)}
    }
  }

  private def applyMove(move: Move): Game = {
    val movingPiece = this.board.pieces(move.from.id)
    val pieces = this.board.pieces.updated(move.from.id, None).updated(move.to.id, movingPiece)
    val newMoves = move +: moves
    val moved = this.copy(nextMove = enemy, board = Board(pieces), moves = newMoves, enPassantTarget = move.enPassantTarget)
    move.implication.map(moved.applyMove).getOrElse(moved)
  }

  lazy val forClient: Map[String, Any] = Map(
    "id" -> id.toString,
    "white" -> white.gPlusId,
    "black" -> black.gPlusId,
    "fen" -> fen,
    "activeColour" -> s"$activeColour",
    "moves" -> moves.map(_.forClient).toList)
}

object Game {
  def apply(id: Int, white: User, black: User): Game = Game(id, white, black, white)

  val openingPosition = Vector[(RankAndFile, Piece)](
    A1 -> WhiteRook, B1 -> WhiteKnight, C1 -> WhiteBishop, D1 -> WhiteQueen,
    E1 -> WhiteKing, F1 -> WhiteBishop, G1 -> WhiteKnight, H1 -> WhiteRook,
    A2 -> WhitePawn, B2 -> WhitePawn, C2 -> WhitePawn, D2 -> WhitePawn,
    E2 -> WhitePawn, F2 -> WhitePawn, G2 -> WhitePawn, H2 -> WhitePawn,
    A7 -> BlackPawn, B7 -> BlackPawn, C7 -> BlackPawn, D7 -> BlackPawn,
    E7 -> BlackPawn, F7 -> BlackPawn, G7 -> BlackPawn, H7 -> BlackPawn,
    A8 -> BlackRook, B8 -> BlackKnight, C8 -> BlackBishop, D8 -> BlackQueen,
    E8 -> BlackKing, F8 -> BlackBishop, G8 -> BlackKnight, H8 -> BlackRook
  ).foldLeft(Vector.fill(64)(None: Option[Piece])){(arr, next) =>
    arr.updated(next._1.id, Some(next._2))
  }

  def buildFrom(row: GameRow): Game = {
    val startGame = Game(row.id.get, user(row.whiteId), user(row.blackId))
    row.moves.grouped(4).map{str: String =>
      val rfs = str.grouped(2).map(RankAndFile.withName).toSeq
      (rfs.head, rfs.tail.head)
    }.foldRight(startGame){case ((from, to), game) =>
      game.move(game.nextMove, from, to).right.get
    }
  }
}

sealed trait CastlingAvailability {
  lazy val betweenRookAndKing = {
    def collect(next: Option[RankAndFile], found: Seq[RankAndFile]): Seq[RankAndFile] = next match {
      case Some(rf) if rf != rookStarts => collect(rf.towards(side), rf +: found)
      case _ => found
    }
    collect(kingMoves.head.towards(side), Seq.empty)
  }

  val colour: Colour
  val fen: Char
  val kingMoves: Seq[RankAndFile]
  val rookStarts: RankAndFile
  val side: BoardSide
}
case object WhiteKingSide extends White with CastlingAvailability {
  val (fen, kingMoves, rookStarts, side) = ('K', Seq(E1, F1, G1), H1, KingSide)
}
case object WhiteQueenSide extends White with CastlingAvailability {
  val (fen, kingMoves, rookStarts, side) = ('Q', Seq(E1, D1, C1), A1, QueenSide)
}
case object BlackKingSide extends Black with CastlingAvailability {
  val (fen, kingMoves, rookStarts, side) = ('k', Seq(E8, F8, G8), H8, KingSide)
}
case object BlackQueenSide extends Black with CastlingAvailability {
  val (fen, kingMoves, rookStarts, side) = ('q', Seq(E8, D8, C8), A8, QueenSide)
}

case class Castling(permitted: Set[CastlingAvailability] =
                    Set(WhiteKingSide, WhiteQueenSide, BlackKingSide, BlackQueenSide)) {

  lazy val flag = permitted.map(_.fen).mkString.sorted match {
    case "" => "-"
    case s => s
  }

  def availabilityFor(colour: Colour) = permitted.filter(ca => ca.colour == colour)
}
