package forkpin

import org.specs2.Specification
import RankAndFile._

class RankAndFileSpec extends Specification { def is = s2"""

  A square in the middle should
    resolve along file to black side $e1
    resolve along file to white side $e2
    resolve along rank to king side $e3
    resolve along rank to queen side $e4
    resolve given a composite direction $e5
    calculate how many squares are blackside $e6
    calculate how many squares are whiteside $e7
    calculate how many squares are kingside $e8
    calculate how many squares are queenside $e9
    calculate how many squares are blackqueenside $e10
    calculate how many squares are blackkingside $e11
    calculate how many squares are whitequeenside $e12
    calculate how many squares are whitekingside $e13

  A square at the white queen side corner should
    resolve away from the edge along rank $edge01
    resolve away from the edge along file $edge02
    not resolve towards the edge along rank $edge03
    not resolve towards the edge along file $edge04
    calculate how many squares are blackside $edge10
    calculate how many squares are whiteside $edge11
    calculate how many squares are kingside $edge12
    calculate how many squares are queenside $edge13
    calculate how many squares are blackqueenside $edge14
    calculate how many squares are blackkingside $edge15
    calculate how many squares are whitequeenside $edge16
    calculate how many squares are whitekingside $edge17

  A square at the black king side corner should
    not resolve towards the edge along rank $edge05
    not resolve towards the edge along file $edge06
    resolve away from the edge along rank $edge07
    resolve away from the edge along file $edge08
    calculate how many squares are blackside $edge20
    calculate how many squares are whiteside $edge21
    calculate how many squares are kingside $edge22
    calculate how many squares are queenside $edge23
    calculate how many squares are blackqueenside $edge24
    calculate how many squares are blackkingside $edge25
    calculate how many squares are whitequeenside $edge26
    calculate how many squares are whitekingside $edge27

  two squares on the backslash diagonal should
    report that they are on the backslash $backslash1
    report that they are not on the forwardslash $backslash2
    report that they are not on the file $backslash3
    report that they are not on the rank $backslash4
    report that they are not on the rook lines $backslash5
    report that they are on the bishop lines $backslash6
    report that they are on the queen lines $backslash7

  two squares on the forwardslash diagonal should
    report that they are not on the backslash $forwardslash1
    report that they are on the forwardslash $forwardslash2
    report that they are not on the file $forwardslash3
    report that they are not on the rank $forwardslash4
    report that they are not on the rook lines $forwardslash5
    report that they are on the bishop lines $forwardslash6
    report that they are on the queen lines $forwardslash7


  """

  def e1 = D5 towards BlackSide must beSome(D6)
  def e2 = D5 towards WhiteSide must beSome(D4)
  def e3 = D5 towards KingSide must beSome(E5)
  def e4 = D5 towards QueenSide must beSome(C5)
  def e5 = D5 towards KingSide + BlackSide + BlackSide must beSome(E7)
  def e6 = D5 squaresInDirection BlackSide must beEqualTo(3)
  def e7 = D5 squaresInDirection WhiteSide must beEqualTo(4)
  def e8 = D5 squaresInDirection KingSide must beEqualTo(4)
  def e9 = D5 squaresInDirection QueenSide must beEqualTo(3)
  def e10 = D5 squaresInDirection BlackSide + QueenSide must beEqualTo(3)
  def e11 = D5 squaresInDirection BlackSide + KingSide must beEqualTo(3)
  def e12 = D5 squaresInDirection WhiteSide + QueenSide must beEqualTo(3)
  def e13 = D5 squaresInDirection WhiteSide + KingSide must beEqualTo(4)

  def edge01 = A1 towards KingSide must beSome(B1)
  def edge02 = A1 towards BlackSide must beSome(A2)
  def edge03 = A1 towards QueenSide must beNone
  def edge04 = A1 towards WhiteSide must beNone
  def edge10 = A1 squaresInDirection BlackSide must beEqualTo(7)
  def edge11 = A1 squaresInDirection WhiteSide must beEqualTo(0)
  def edge12 = A1 squaresInDirection KingSide must beEqualTo(7)
  def edge13 = A1 squaresInDirection QueenSide must beEqualTo(0)
  def edge14 = A1 squaresInDirection BlackSide + QueenSide must beEqualTo(0)
  def edge15 = A1 squaresInDirection BlackSide + KingSide must beEqualTo(7)
  def edge16 = A1 squaresInDirection WhiteSide + QueenSide must beEqualTo(0)
  def edge17 = A1 squaresInDirection WhiteSide + KingSide must beEqualTo(0)

  def edge05 = H8 towards KingSide must beNone
  def edge06 = H8 towards BlackSide must beNone
  def edge07 = H8 towards QueenSide must beSome(G8)
  def edge08 = H8 towards WhiteSide must beSome(H7)
  def edge20 = H8 squaresInDirection BlackSide must beEqualTo(0)
  def edge21 = H8 squaresInDirection WhiteSide must beEqualTo(7)
  def edge22 = H8 squaresInDirection KingSide must beEqualTo(0)
  def edge23 = H8 squaresInDirection QueenSide must beEqualTo(7)
  def edge24 = H8 squaresInDirection BlackSide + QueenSide must beEqualTo(0)
  def edge25 = H8 squaresInDirection BlackSide + KingSide must beEqualTo(0)
  def edge26 = H8 squaresInDirection WhiteSide + QueenSide must beEqualTo(7)
  def edge27 = H8 squaresInDirection WhiteSide + KingSide must beEqualTo(0)

  def backslash1 = F4 onSameBackSlashDiagonalAs C7 must beTrue
  def backslash2 = F4 onSameForwardSlashDiagonalAs C7 must beFalse
  def backslash3 = F4 onSameRankAs C7 must beFalse
  def backslash4 = F4 onSameFileAs C7 must beFalse
  def backslash5 = F4 onSameRookMovementAs C7 must beFalse
  def backslash6 = F4 onSameBishopMovementAs C7 must beTrue
  def backslash7 = F4 onSameQueenMovementAs C7 must beTrue

  def forwardslash1 = C1 onSameBackSlashDiagonalAs H6 must beFalse
  def forwardslash2 = C1 onSameForwardSlashDiagonalAs H6 must beTrue
  def forwardslash3 = C1 onSameFileAs H6 must beFalse
  def forwardslash4 = C1 onSameRankAs H6 must beFalse
  def forwardslash5 = C1 onSameRookMovementAs H6 must beFalse
  def forwardslash6 = C1 onSameBishopMovementAs H6 must beTrue
  def forwardslash7 = C1 onSameQueenMovementAs H6 must beTrue

}
