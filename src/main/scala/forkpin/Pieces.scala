package forkpin

import RankAndFile._

sealed trait San {
  def sanModifier(c: Char): Char
}

trait Role extends San { // todo - check pandolfini for better name
  def validMoves(rf: RankAndFile, board: Board): Set[Move]
  val sanRole: Char
  val forward: Side
  val opposite: Colour
}
trait Pawn extends Role {
  val sanRole = 'p'
  def validMoves(rf: RankAndFile, board: Board) = {
    val depth = (rf.id / 7, forward) match {
      // todo - if depth 2, then set the en passant target
      case (1, WhiteSide) => 2
      case (7, BlackSide) => 2
      case _ => 1
    }
    val captures: Set[Move] = Set(forward + QueenSide, forward + KingSide).flatMap(rf.towards).filter { pos =>
      board.colourAt(pos) == Some(opposite)
    }.map(to => Move(rf, to, Some(to)))
    rf.seek(board, depth, forward) ++ captures
  }
}
trait Knight extends Role {
  val sanRole = 'n'
  def validMoves(rf: RankAndFile, board: Board) = rf.seek(board, 1,
    KingSide + BlackSide + BlackSide, KingSide + KingSide + BlackSide,
    KingSide + WhiteSide + WhiteSide, KingSide + KingSide + WhiteSide,
    QueenSide + BlackSide + BlackSide, QueenSide + QueenSide + BlackSide,
    QueenSide + WhiteSide + WhiteSide, QueenSide + QueenSide + WhiteSide)
}
trait Bishop extends Role {
  val sanRole = 'b'
  def validMoves(rf: RankAndFile, board: Board) = rf.seek(board,
    KingSide + BlackSide, KingSide + WhiteSide, QueenSide + BlackSide, QueenSide + WhiteSide)
}
trait Rook extends Role {
  val sanRole = 'r'
  def validMoves(rf: RankAndFile, board: Board) = rf.seek(board, KingSide, QueenSide, BlackSide, WhiteSide)
}
trait Queen extends Role {
  val sanRole = 'q'
  def validMoves(rf: RankAndFile, board: Board) = rf.seek(board,
    KingSide, QueenSide, BlackSide, WhiteSide, KingSide + BlackSide,
    KingSide + WhiteSide, QueenSide + BlackSide, QueenSide + WhiteSide)
}
trait King extends Role {
  val sanRole = 'k'
  def validMoves(rf: RankAndFile, board: Board) = rf.seek(board, 1,
    KingSide, QueenSide, BlackSide, WhiteSide, KingSide + BlackSide,
    KingSide + WhiteSide, QueenSide + BlackSide, QueenSide + WhiteSide) // todo - add castling
}

sealed trait Colour extends San {
  val sanColour: Char
  val colour: Colour
  val opposite: Colour
  val forward: Side
}
trait Black extends Colour {
  val sanColour = 'b'
  def sanModifier(c: Char): Char = c
  val colour = Black
  val opposite = White
  val forward = WhiteSide
}
case object Black extends Black
trait White extends Colour {
  val sanColour = 'w'
  def sanModifier(c: Char): Char = c.toUpper
  val colour = White
  val opposite = Black
  val forward = BlackSide
}
case object White extends White

trait Piece extends Role with Colour // todo - check pandolfini for better name

case object WhitePawn extends Piece with White with Pawn
case object WhiteKnight extends Piece with White with Knight
case object WhiteBishop extends Piece with White with Bishop
case object WhiteRook extends Piece with White with Rook
case object WhiteQueen extends Piece with White with Queen
case object WhiteKing extends Piece with White with King
case object BlackPawn extends Piece with Black with Pawn
case object BlackKnight extends Piece with Black with Knight
case object BlackBishop extends Piece with Black with Bishop
case object BlackRook extends Piece with Black with Rook
case object BlackQueen extends Piece with Black with Queen
case object BlackKing extends Piece with Black with King

trait Side {
  val offset: Int
  def +(side: Side) = new Object with Side {
    val offset: Int = Side.this.offset + side.offset
  }
}
case object QueenSide extends Side {
  val offset = -1
}
case object KingSide extends Side {
  val offset = 1
}
case object BlackSide extends Side {
  val offset = -8
}
case object WhiteSide extends Side {
  val offset = 8
}
