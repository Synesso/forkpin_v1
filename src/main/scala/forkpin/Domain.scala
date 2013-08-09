package forkpin

import scala.Some
import forkpin.Persistent.{GameRow, User, user}


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
}
import RankAndFile._

case class Game(id: Int, white: User, black: User,
                nextMove: User,
                pieces: Array[Option[Piece]] = Game.openingPosition,
                castling: Castling = Castling(),
                enPassantTarget: Option[RankAndFile] = None,
                halfMoveClock: Int = 0,
                fullMove: Int = 1,
                moves: Vector[Move] = Vector.empty[Move]) {

  lazy val san = pieces.grouped(8).map{array =>
    val (row, nones) = array.foldLeft(("", 0)) {case ((str, noneCount), maybePiece) =>
      maybePiece.map{
        piece => (s"$str${nonZero(noneCount)}${piece.san}", 0)
      }.getOrElse{
        (str, noneCount + 1)
      }
    }
    s"$row${nonZero(nones)}"
  }.mkString("/")
  private def nonZero(i: Int) = if (i == 0) "" else i.toString

  lazy val activeColour = if (nextMove equals white) 'w' else 'b'

  lazy val enPassantTargetFlag = enPassantTarget.map(_.toString.toLowerCase).getOrElse("-")

  lazy val fen = s"$san $activeColour ${castling.flag} $enPassantTargetFlag $halfMoveClock $fullMove"

  lazy val pickledMoves = moves.map{m => m.from.toString + m.to.toString}.mkString

  def move(from: String, to: String): Either[InvalidMove, Game] = move(Move(RankAndFile.withName(from), RankAndFile.withName(to)))
  def move(move: Move): Either[InvalidMove, Game] = Right(this) // todo - apply the move - various rules to apply.

  lazy val forClient: Map[String, String] = Map("id" -> id.toString, "white" -> white.gPlusId, "black" -> black.gPlusId, "fen" -> fen)
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
  ).foldLeft(Array.fill(64)(None: Option[Piece])){(arr, next) => 
    arr.updated(next._1.id, Some(next._2))
  }

  def buildFrom(row: GameRow): Game = {
    val startGame = Game(row.id.get, user(row.whiteId), user(row.blackId))
    row.moves.grouped(4).map{str: String =>
      val rfs = str.grouped(2).map(RankAndFile.withName).toSeq
      Move(rfs.head, rfs.tail.head)
    }.foldRight(startGame){(move, game) =>
      game.move(move).right.get
    }
  }

}

case class Move(from: RankAndFile, to: RankAndFile)

case class InvalidMove(game: Game, user: User, move: Move)

case class Castling(sides: Seq[Piece] = Seq(WhiteKing, WhiteQueen, BlackKing, BlackQueen)) {

  lazy val flag = sides match {
    case Nil => "-"
    case _ => sides.map(_.san).mkString
  }
}

class Piece(val san: Char)
case object WhitePawn extends Piece('P')
case object WhiteKnight extends Piece('N')
case object WhiteBishop extends Piece('B')
case object WhiteRook extends Piece('R')
case object WhiteQueen extends Piece('Q')
case object WhiteKing extends Piece('K')
case object BlackPawn extends Piece('p')
case object BlackKnight extends Piece('n')
case object BlackBishop extends Piece('b')
case object BlackRook extends Piece('r')
case object BlackQueen extends Piece('q')
case object BlackKing extends Piece('k')
