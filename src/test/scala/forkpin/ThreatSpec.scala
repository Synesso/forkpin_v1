package forkpin

import RankAndFile._
import forkpin.persist.Persistent
import Persistent.User
import org.specs2.{ScalaCheck, Specification}
import org.scalacheck.{Gen, Arbitrary}
import org.specs2.mock.Mockito

class ThreatSpec extends Specification with ScalaCheck with TestImplicits with Mockito { def is = s2"""

  Any square should
    have $noThreatOnEmptyBoard

  A queen should
    threaten on the rank, file or diagonal $queenOnRankFileOrDiagonal
    not threaten when blocked $blockedQueen

  A king should
    threaten on the adjacent square $kingOnTheNextSquare
    not threaten on a non-adjacent square $kingOnANonAdjacentSquare

  A rook should
    threaten on the rank or file $rookOnRankOrFile
    not threaten when blocked $blockedRook

  A bishop should
    threaten on the diagonal $bishopOnDiagonal
    not threaten when blocked $blockedBishop

  A knight should
    threaten on the dogleg $knightOnDogleg
    threaten even when square is protected $blockedKnight

  A friendly piece should
    never threaten $friendlyPieces


"""

  def noThreatOnEmptyBoard = "no threat on an empty board" ! prop {(square: RankAndFile) =>
    emptyGame.isThreatenedAt(square) must beFalse
  }

  def queenOnRankFileOrDiagonal = prop {
    (square: RankAndFile, enemySquare: RankAndFile) =>
    val threat = square != enemySquare && square.onSameQueenMovementAs(enemySquare)
    game(enemySquare -> BlackQueen).isThreatenedAt(square) must beEqualTo(threat)
  }

  def friendlyPieces = prop{
    (square: RankAndFile, friendlySquare: RankAndFile, role: RoleMarker) =>
      game(friendlySquare -> White.sided(role)).isThreatenedAt(square) must beFalse
  }

  def blockedQueen = arbitraryQueenAttackSquares{(squaresTriple: (RankAndFile, RankAndFile, RankAndFile)) =>
    val squares = Seq(squaresTriple._1, squaresTriple._2, squaresTriple._3).sorted
    val blockedQueenGame = game().place(squares(1), BlackKnight).place(squares(2), BlackQueen)
    blockedQueenGame.isThreatenedAt(squares(0)) must beFalse
  }

  def kingOnTheNextSquare = arbitraryAdjacentSquares{
    case (first: RankAndFile, second: RankAndFile) =>
      game(first -> BlackKing).isThreatenedAt(second) must beTrue
  }

  def kingOnANonAdjacentSquare = arbitraryNonAdjacentSquares{
    case (first: RankAndFile, second: RankAndFile) =>
      game(first -> BlackKing).isThreatenedAt(second) must beFalse
  }

  def rookOnRankOrFile = prop {(square: RankAndFile, enemySquare: RankAndFile) =>
    val threat = square != enemySquare && square.onSameRookMovementAs(enemySquare)
    game(enemySquare -> BlackRook).isThreatenedAt(square) must beEqualTo(threat)
  }

  def blockedRook = arbitraryRookAttackSquares{case (first: RankAndFile, second: RankAndFile, third: RankAndFile) =>
    val squares = Seq(first, second, third).sorted
    val blockedRookGame = game().place(squares(1), BlackKnight).place(squares(2), BlackRook)
    blockedRookGame.isThreatenedAt(squares(0)) must beFalse
  }

  def bishopOnDiagonal = prop {(square: RankAndFile, enemySquare: RankAndFile) =>
    val threat = square != enemySquare && square.onSameBishopMovementAs(enemySquare)
    game(enemySquare -> BlackBishop).isThreatenedAt(square) must beEqualTo(threat)
  }

  def blockedBishop = arbitraryBishopAttackSquares{case (first: RankAndFile, second: RankAndFile, third: RankAndFile) =>
    val squares = Seq(first, second, third).sorted
    val blockedBishopGame = game().place(squares(1), BlackKnight).place(squares(2), BlackBishop)
    blockedBishopGame.isThreatenedAt(squares(0)) must beFalse
  }

  def knightOnDogleg = arbitraryKnightAttackSquares{case (attacker: RankAndFile, threatened: RankAndFile) =>
    game(attacker -> BlackKnight).isThreatenedAt(threatened) must beTrue
  }

  def blockedKnight = arbitraryKnightAttackSquares{case (attacker: RankAndFile, threatened: RankAndFile) =>
    val thisGame = threatened.surroundingSquares.foldLeft(game(attacker -> BlackKnight)){(g: Game, rf: RankAndFile) =>
      g.place(rf -> WhitePawn)
    }
    thisGame.isThreatenedAt(threatened) must beTrue
  }

  val squares = Gen.oneOf(RankAndFile.values.toSeq)
  val roles = Gen.oneOf(Seq(Pawn, Knight, Bishop, Rook, Queen, King))
  val pieces = for {
    role <- roles
    colour <- Gen.oneOf(Seq(Black, White))
  } yield colour sided role
  val sides: Gen[BoardSide] = Gen.oneOf(Seq(BlackSide, WhiteSide, QueenSide, KingSide))

  implicit val arbitrarySquare = Arbitrary(squares)
  implicit val arbitraryRole = Arbitrary(roles)
  implicit val arbitraryPiece = Arbitrary(pieces)

  implicit val arbitraryQueenAttackSquares = Arbitrary(arbitraryAttackSquares((square1: RankAndFile, square2: RankAndFile) =>
    square1.onSameQueenMovementAs(square2)))

  implicit val arbitraryRookAttackSquares = Arbitrary(arbitraryAttackSquares((square1: RankAndFile, square2: RankAndFile) =>
    square1.onSameRookMovementAs(square2)))

  implicit val arbitraryBishopAttackSquares = Arbitrary(arbitraryAttackSquares((square1: RankAndFile, square2: RankAndFile) =>
    square1.onSameBishopMovementAs(square2)))

  implicit val arbitraryKnightAttackSquares = Arbitrary(for {
    square <- squares
    twoStep <- sides
    oneStep <- Gen.oneOf(twoStep.adjacent.toSeq) suchThat (step => square.towards(step + twoStep + twoStep).isDefined)
  } yield (square, square.towards(oneStep + twoStep + twoStep).get))

  def arbitraryAttackSquares(p: (RankAndFile, RankAndFile) => Boolean): Gen[(RankAndFile, RankAndFile, RankAndFile)] = {
    for {
      square1 <- squares
      square2 <- Gen.oneOf((RankAndFile.lines(p(square1, _)) - square1).toSeq)
      square3 <- Gen.oneOf((square1.lineOf(square2) - square1 - square2).toSeq)
    } yield (square1, square2, square3)
  }

  val arbitraryAdjacentSquares = Arbitrary(for {
    first <- squares
    second <- Gen.oneOf(first.surroundingSquares)
  } yield (first, second))

  val arbitraryNonAdjacentSquares = Arbitrary(for {
    first <- squares
    second <- Gen.oneOf((RankAndFile.values -- first.surroundingSquares - first).toSeq)
  } yield (first, second))

  val (white, black) = (mock[User], mock[User])

  def game(pieces: (RankAndFile, Piece)*): Game = {
    val b = Board(pieces = pieces.foldLeft(Vector.fill(64)(None: Option[Piece])){(arr, next) =>
      arr.updated(next._1.id, Some(next._2))
    })
    Game(1, white, black, white, board = b)
  }

  val emptyGame = game()

}
