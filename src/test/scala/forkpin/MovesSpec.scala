package forkpin

import RankAndFile._
import org.specs2.Specification

class MovesSpec extends Specification with TestImplicits { def is = s2"""

  A rook should
    move along its own rank and file only $rook1
    include and stop at enemy pieces & exclude and stop at friendly pieces $rook2

  A knight should
    move in a dogleg $knight1
    include enemy pieces $knight2
    exclude friendly pieces $knight3

  A bishop should
    move diagonally $bishop1
    include and stop at enemy pieces & exclude and stop at friendly pieces $bishop2

  A queen should
    move along rank, file and diagonally $queen1
    include and stop at enemy pieces & exclude and stop at friendly pieces $queen2

  A king should
    move along rank, file and diagonally to a depth of 1 $king1
    include and stop at enemy pieces $king2
    exclude and stop at friendly pieces $king3
    castle when permitted $castleOk
    not castle king side when already moved $castleKingSideDeniedAlreadyMoved
    not castle king side when piece is in the way $castleKingSideDeniedIntermediatePiece
    not castle king side when king is under threat $castleKingSideDeniedWhenKingUnderThreat
    not castle king side when king is passing through threat $castleKingSideDeniedWhenKingPassingThroughThreat
    not castle king side when king is moving into threat $castleKingSideDeniedWhenKingMovingIntoThreat
    castle king side when rook is under threat $castleKingSideOkWhenRookIsUnderThreat
    not castle queen side when already moved $castleQueenSideDeniedAlreadyMoved
    not castle queen side when piece is in the way $castleQueenSideDeniedIntermediatePiece
    not castle queen side when king is under threat $castleQueenSideDeniedWhenKingUnderThreat
    not castle queen side when king is passing through threat $castleQueenSideDeniedWhenKingPassingThroughThreat
    not castle queen side when king is moving into threat $castleQueenSideDeniedWhenKingMovingIntoThreat
    castle queen side when rook is under threat $castleQueenSideOkWhenRookIsUnderThreat
    castle queen side when rook passes through threat $castleQueenSideOkWhenRookPassesThroughThreat

  A white pawn should
    move forward 1 or 2 squares from 2nd rank $whitePawnFirstMove
    move forward 1 square from other ranks $whitePawnSubsequentMove
    take diagonally forward, queenside $whitePawnTakeQueenSide
    take diagonally forward, kingside $whitePawnTakeKingSide
    take diagonally forward only, forking $whitePawnTakeEitherSide
    take diagonally or move 2 squares from 2nd rank $whitePawnTakeOrTwoMoves
    become blocked by enemy pieces on the file $whitePawnBlockedByEnemy
    become blocked by friendly pieces on the file $whitePawnBlockedByFriend
    take diagonally even when blocked on the file $whitePawnBlockedCanStillTake
    take in passing $whiteEnPassant

  A black pawn should
    move forward 1 or 2 squares from 2nd rank $blackPawnFirstMove
    move forward 1 square from other ranks $blackPawnSubsequentMove
    take diagonally forward, queenside $blackPawnTakeQueenSide
    take diagonally forward, kingside $blackPawnTakeKingSide
    take diagonally forward only, forking $blackPawnTakeEitherSide
    take diagonally or move 2 squares from 2nd rank $blackPawnTakeOrTwoMoves
    become blocked by enemy pieces on the file $blackPawnBlockedByEnemy
    become blocked by friendly pieces on the file $blackPawnBlockedByFriend
    take diagonally even when blocked on the file $blackPawnBlockedCanStillTake
    take in passing $blackEnPassant

  """

  def rook1 = WhiteRook.validMoves(D6, emptyGame).map(_.to) must containTheSameElementsAs(
    Seq(D1, D2, D3, D4, D5, D7, D8, A6, B6, C6, E6, F6, G6, H6))

  def rook2 = BlackRook.validMoves(E4, startGame).map(_.to) must containTheSameElementsAs(
    Seq(E2, E3, E5, E6, A4, B4, C4, D4, F4, G4, H4)
  )

  def knight1 = WhiteKnight.validMoves(D5, emptyGame).map(_.to) must containTheSameElementsAs(
    Seq(B4, B6, C3, C7, E3, E7, F4, F6)
  )

  def knight2 = BlackKnight.validMoves(C3, startGame).map(_.to) must containTheSameElementsAs(
    Seq(A2, A4, B1, B5, D1, D5, E2, E4)
  )

  def knight3 = BlackKnight.validMoves(B8, startGame).map(_.to) must containTheSameElementsAs(
    Seq(A6, C6)
  )

  def bishop1 = WhiteBishop.validMoves(F6, emptyGame).map(_.to) must containTheSameElementsAs(
    Seq(E5, E7, D4, D8, C3, B2, A1, G5, G7, H4, H8)
  )

  def bishop2 = WhiteBishop.validMoves(F6, startGame).map(_.to) must containTheSameElementsAs(
    Seq(E5, E7, D4, C3, G5, G7, H4)
  )

  def queen1 = BlackQueen.validMoves(F2, emptyGame).map(_.to) must containTheSameElementsAs(
    Seq(F1, F3, F4, F5, F6, F7, F8, A2, B2, C2, D2, E2, G2, H2, E1, E3, D4, C5, B6, A7, G1, G3, H4)
  )

  def queen2 = BlackQueen.validMoves(F2, startGame).map(_.to) must containTheSameElementsAs(
    Seq(E1, F1, G1, E2, G2, E3, D4, C5, B6, G3, H4, F3, F4, F5, F6)
  )

  def king1 = WhiteKing.validMoves(B4, emptyGame).map(_.to) must containTheSameElementsAs(
    Seq(A3, B3, C3, C4, C5, B5, A5, A4)
  )

  def king2 = WhiteKing.validMoves(B6, startGame).map(_.to) must containTheSameElementsAs(
    Seq(A7, B7, C7, C6, C5, B5, A5, A6)
  )

  def king3 = WhiteKing.validMoves(B3, startGame).map(_.to) must containTheSameElementsAs(
    Seq(A3, A4, B4, C4, C3)
  )

  def castleOk = WhiteKing.validMoves(E1, castleGame).map(_.to) must containAllOf(Seq(C1, G1))

  def castleKingSideDeniedAlreadyMoved = {
    val game = castleGame.copy(castling = Castling(Set(WhiteQueenSide)))
    WhiteKing.validMoves(E1, game).map(_.to) must contain(C1) and not(contain(G1))
  }

  def castleKingSideDeniedIntermediatePiece = {
    val game = castleGame.place(F1 -> WhiteBishop)
    WhiteKing.validMoves(E1, game).map(_.to) must contain(C1) and not(contain(G1))
  }

  def castleQueenSideDeniedAlreadyMoved = {
    val game = castleGame.copy(castling = Castling(Set(WhiteKingSide)))
    WhiteKing.validMoves(E1, game).map(_.to) must contain(G1) and not(contain(C1))
  }

  def castleQueenSideDeniedIntermediatePiece = {
    val game = castleGame.place(D1 -> WhiteBishop)
    WhiteKing.validMoves(E1, game).map(_.to) must contain(G1) and not(contain(C1))
  }

  def whitePawnFirstMove = WhitePawn.validMoves(F2, startGame).map(_.to) must containTheSameElementsAs(Seq(F3, F4))

  def whitePawnSubsequentMove = WhitePawn.validMoves(B5, emptyGame).map(_.to) must containTheSameElementsAs(Seq(B6))

  def whitePawnTakeQueenSide = WhitePawn.validMoves(D5, emptyGame.place(E6 -> BlackQueen)).map(_.to) must
    containTheSameElementsAs(Seq(D6, E6))

  def whitePawnTakeKingSide = WhitePawn.validMoves(D5, emptyGame.place(C6 -> BlackQueen)).map(_.to) must
    containTheSameElementsAs(Seq(D6, C6))

  def whitePawnTakeEitherSide = WhitePawn.validMoves(D5, emptyGame.surround(D5, BlackQueen)).map(_.to) must
    containTheSameElementsAs(Seq(E6, C6))

  def whitePawnTakeOrTwoMoves = WhitePawn.validMoves(C2, emptyGame.place(D3 -> BlackPawn)).map(_.to) must
    containTheSameElementsAs(Seq(D3, C3, C4))

  def whitePawnBlockedByEnemy = WhitePawn.validMoves(C2, emptyGame.place(C3 -> BlackPawn)).map(_.to) must beEmpty

  def whitePawnBlockedByFriend = WhitePawn.validMoves(C2, emptyGame.place(C3 -> WhitePawn)).map(_.to) must beEmpty

  def whitePawnBlockedCanStillTake = WhitePawn.validMoves(C2, emptyGame.place(C3 -> WhitePawn).place(B3 -> BlackRook))
    .map(_.to) must containTheSameElementsAs(Seq(B3))

  def blackPawnFirstMove = BlackPawn.validMoves(F7, startGame).map(_.to) must containTheSameElementsAs(Seq(F6, F5))

  def blackPawnSubsequentMove = BlackPawn.validMoves(B5, emptyGame).map(_.to) must containTheSameElementsAs(Seq(B4))

  def blackPawnTakeQueenSide = BlackPawn.validMoves(D5, emptyGame.place(E4 -> WhiteQueen)).map(_.to) must
    containTheSameElementsAs(Seq(D4, E4))

  def blackPawnTakeKingSide = BlackPawn.validMoves(D5, emptyGame.place(C4 -> WhiteQueen)).map(_.to) must
    containTheSameElementsAs(Seq(D4, C4))

  def blackPawnTakeEitherSide = BlackPawn.validMoves(D5, emptyGame.surround(D5, WhiteQueen)).map(_.to) must
    containTheSameElementsAs(Seq(E4, C4))

  def blackPawnTakeOrTwoMoves = BlackPawn.validMoves(C7, emptyGame.place(D6 -> WhitePawn)).map(_.to) must
    containTheSameElementsAs(Seq(D6, C6, C5))

  def blackPawnBlockedByEnemy = BlackPawn.validMoves(C7, emptyGame.place(C6 -> WhitePawn)).map(_.to) must beEmpty

  def blackPawnBlockedByFriend = BlackPawn.validMoves(C7, emptyGame.place(C6 -> BlackPawn)).map(_.to) must beEmpty

  def blackPawnBlockedCanStillTake = BlackPawn.validMoves(C7, emptyGame.place(C6 -> BlackPawn).place(B6 -> WhiteRook))
    .map(_.to) must containTheSameElementsAs(Seq(B6))

  def castleKingSideDeniedWhenKingUnderThreat =
    WhiteKing.validMoves(E1, castleGame.place(E8 -> BlackRook)).map(_.to) must not contain G1

  def castleKingSideDeniedWhenKingPassingThroughThreat =
    WhiteKing.validMoves(E1, castleGame.place(F8 -> BlackRook)).map(_.to) must not contain G1

  def castleKingSideDeniedWhenKingMovingIntoThreat =
    WhiteKing.validMoves(E1, castleGame.place(G8 -> BlackRook)).map(_.to) must not contain G1

  def castleKingSideOkWhenRookIsUnderThreat =
    WhiteKing.validMoves(E1, castleGame.place(H8 -> BlackRook)).map(_.to) must contain(G1)

  def castleQueenSideDeniedWhenKingUnderThreat =
    WhiteKing.validMoves(E1, castleGame.place(E8 -> BlackRook)).map(_.to) must not contain C1

  def castleQueenSideDeniedWhenKingPassingThroughThreat =
    WhiteKing.validMoves(E1, castleGame.place(D8 -> BlackRook)).map(_.to) must not contain C1

  def castleQueenSideDeniedWhenKingMovingIntoThreat =
    WhiteKing.validMoves(E1, castleGame.place(C8 -> BlackRook)).map(_.to) must not contain C1

  def castleQueenSideOkWhenRookPassesThroughThreat =
    WhiteKing.validMoves(E1, castleGame.place(B8 -> BlackRook)).map(_.to) must contain(C1)

  def castleQueenSideOkWhenRookIsUnderThreat =
    WhiteKing.validMoves(E1, castleGame.place(A8 -> BlackRook)).map(_.to) must contain(C1)

  def whiteEnPassant = WhitePawn.validMoves(G5, startGame.copy(enPassantTarget = Some(F6))).map(_.to) must contain(F6)

  def blackEnPassant = BlackPawn.validMoves(D4, startGame.copy(enPassantTarget = Some(C3))).map(_.to) must contain(C3)
}
