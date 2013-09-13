package forkpin

import RankAndFile._

sealed trait San {
  def sanModifier(c: Char): Char
}

trait Role extends San {
  def validMoves(rf: RankAndFile, game: Game): Set[Move]
  def validCapturingMoves(rf: RankAndFile, game: Game): Set[Move] = validMoves(rf, game)
  def is(aCertainColour: Colour) = colour == aCertainColour
  val sanRole: Char
  val forward: BoardSide
  val colour: Colour
  val opposite: Colour
}
trait RoleMarker

trait Pawn extends Role {
  val sanRole = 'p'
  def validMoves(rf: RankAndFile, game: Game) = {
    val depth = (rf.id / 8, forward) match {
      case (1, WhiteSide) => 2
      case (6, BlackSide) => 2
      case _ => 1
    }
    val forwards: Set[Move] = rf.seek(game, colour, depth, allowCapture = false, forward).map{m =>
      if (math.abs(m.from.id - m.to.id) == 16) m.copy(enPassantTarget = m.from.towards(forward))
      else m
    }
    val captures: Set[Move] = validCapturingMoves(rf, game)
    forwards ++ captures
  }

  override def validCapturingMoves(rf: RankAndFile, game: Game) =
    Set(forward + QueenSide, forward + KingSide).flatMap(rf.towards).flatMap { pos =>
      if (game.board.colourAt(pos) == Some(opposite)) Some(Move(rf, pos, Some(pos)))
      else if (game.enPassantTarget == Some(pos)) Some(Move(rf, pos, pos.towards(opposite.forward)))
      else None
    }

}
object Pawn extends RoleMarker
trait Knight extends Role {
  val sanRole = 'n'
  def validMoves(rf: RankAndFile, game: Game) = rf.seek(game, colour, 1,
    KingSide + BlackSide + BlackSide, KingSide + KingSide + BlackSide,
    KingSide + WhiteSide + WhiteSide, KingSide + KingSide + WhiteSide,
    QueenSide + BlackSide + BlackSide, QueenSide + QueenSide + BlackSide,
    QueenSide + WhiteSide + WhiteSide, QueenSide + QueenSide + WhiteSide)
}
object Knight extends RoleMarker
trait Bishop extends Role {
  val sanRole = 'b'
  def validMoves(rf: RankAndFile, game: Game) = rf.seek(game, colour,
    KingSide + BlackSide, KingSide + WhiteSide, QueenSide + BlackSide, QueenSide + WhiteSide)
}
object Bishop extends RoleMarker
trait Rook extends Role {
  val sanRole = 'r'
  def validMoves(rf: RankAndFile, game: Game) = rf.seek(game, colour, KingSide, QueenSide, BlackSide, WhiteSide)
}
object Rook extends RoleMarker
trait Queen extends Role {
  val sanRole = 'q'
  def validMoves(rf: RankAndFile, game: Game) = rf.seek(game, colour,
    KingSide, QueenSide, BlackSide, WhiteSide, KingSide + BlackSide,
    KingSide + WhiteSide, QueenSide + BlackSide, QueenSide + WhiteSide)
}
object Queen extends RoleMarker
trait King extends Role {
  val sanRole = 'k'

  override def validCapturingMoves(rf: RankAndFile, game: Game): Set[Move] = rf.seek(game, colour, 1,
    KingSide, QueenSide, BlackSide, WhiteSide, KingSide + BlackSide,
    KingSide + WhiteSide, QueenSide + BlackSide, QueenSide + WhiteSide)

  def validMoves(rf: RankAndFile, game: Game) = {
    val castlingMoves = {
      game.castling.availabilityFor(colour).filter{ca =>
        game.board.pieceAt(ca.rookStarts).exists(_ == colour.sided(Rook)) &&
        ca.betweenRookAndKing.forall(rf => !game.isOccupiedAt(rf)) &&
        ca.kingMoves.forall{rf => !game.isThreatenedAt(rf)}
      }.map{ca => Move(rf, ca.kingMoves.last, implication = Some(Move(ca.rookStarts, ca.kingMoves(1))))}
    }.toSet

    castlingMoves ++ validCapturingMoves(rf, game)
  }
}
object King extends RoleMarker

sealed trait Colour extends San {
  val sanColour: Char
  val colour: Colour
  val opposite: Colour
  val forward: BoardSide
  def sided(role: RoleMarker): Piece
}
trait Black extends Colour {
  val sanColour = 'b'
  def sanModifier(c: Char): Char = c
  val colour = Black
  val opposite = White
  val forward = WhiteSide
  def sided(role: RoleMarker) = role match {
    case Pawn => BlackPawn
    case Knight => BlackKnight
    case Bishop => BlackBishop
    case Rook => BlackRook
    case Queen => BlackQueen
    case King => BlackKing
  }
}
case object Black extends Black
trait White extends Colour {
  val sanColour = 'w'
  def sanModifier(c: Char): Char = c.toUpper
  val colour = White
  val opposite = Black
  val forward = BlackSide
  def sided(role: RoleMarker) = role match {
    case Pawn => WhitePawn
    case Knight => WhiteKnight
    case Bishop => WhiteBishop
    case Rook => WhiteRook
    case Queen => WhiteQueen
    case King => WhiteKing
  }
}
case object White extends White

trait Piece extends Role with Colour
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

