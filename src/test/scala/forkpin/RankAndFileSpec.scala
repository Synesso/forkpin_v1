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

  A square at the white queen side corner should
    resolve away from the edge along rank $edge01
    resolve away from the edge along file $edge02
    not resolve towards the edge along rank $edge03
    not resolve towards the edge along file $edge04

  A square at the black king side corner should
    not resolve towards the edge along rank $edge05
    not resolve towards the edge along file $edge06
    resolve away from the edge along rank $edge07
    resolve away from the edge along file $edge08

  """

  def e1 = D5 towards BlackSide must beSome(D6)
  def e2 = D5 towards WhiteSide must beSome(D4)
  def e3 = D5 towards KingSide must beSome(E5)
  def e4 = D5 towards QueenSide must beSome(C5)
  def e5 = D5 towards KingSide + BlackSide + BlackSide must beSome(E7)

  def edge01 = A1 towards KingSide must beSome(B1)
  def edge02 = A1 towards BlackSide must beSome(A2)
  def edge03 = A1 towards QueenSide must beNone
  def edge04 = A1 towards WhiteSide must beNone

  def edge05 = H8 towards KingSide must beNone
  def edge06 = H8 towards BlackSide must beNone
  def edge07 = H8 towards QueenSide must beSome(G8)
  def edge08 = H8 towards WhiteSide must beSome(H7)

  // todo - a rank/file should know its rank and file, not just its ordinal, so that it can detect edge of board


}
