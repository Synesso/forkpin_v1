package forkpin

import org.specs2.{ScalaCheck, Specification}
import org.scalacheck.{Arbitrary, Gen}
import org.specs2.execute.Result

class CastlingAvailabilitySpec extends Specification with ScalaCheck { def is = s2"""

  A non-empty castling object must
    have a flag which is $alphabeticallySorted
    have the $sameSizeAsInputSet
    contain $onlyKorQ
    contain $asManyKsAsKingSides
    contain $asManyQsAsQueenSides
    contain $asManyLowerCaseAsBlack
    contain $asManyUpperCaseAsWhite
    show $availabilityForBlack
    show $availabilityForWhite

  An empty castling object must
    have a flag of '-' $emptyFlagIsDash

"""

  implicit val caSet = Arbitrary(Gen.containerOf1[Set, CastlingAvailability](
    Gen.oneOf(Seq(WhiteKingSide, WhiteQueenSide, BlackKingSide, BlackQueenSide))))

  def alphabeticallySorted = "alphabetically sorted" ! castlingMust(c => c.flag.sorted must_== c.flag)

  def sameSizeAsInputSet = "same size as input set" ! castlingMust(c => c.flag.size must_== c.permitted.size)

  def onlyKorQ = "only ks or qs" !
    castlingMust(c => c.flag.toLowerCase.replaceAll("[k|q]", "") must beEmpty)

  def asManyKsAsKingSides = "as many Ks as king sides" !
    castlingMust(c => c.flag.toLowerCase.count(_ == 'k') must_== c.permitted.count(_.side == KingSide))

  def asManyQsAsQueenSides = "as many Qs as queen sides" !
    castlingMust(c => c.flag.toLowerCase.count(_ == 'q') must_== c.permitted.count(_.side == QueenSide))

  def asManyUpperCaseAsWhite = "as many uppercase as white" !
    castlingMust(c => c.flag.count(_.isLower) == c.permitted.count(_.colour == Black))

  def asManyLowerCaseAsBlack = "as many lowercase as black" !
    castlingMust(c => c.flag.count(_.isUpper) == c.permitted.count(_.colour == White))

  def availabilityForBlack = "availability for black" !
    castlingMust(c => c.availabilityFor(Black) must_== c.permitted.filter(_.colour == Black))

  def availabilityForWhite = "availability for white" !
    castlingMust(c => c.availabilityFor(White) must_== c.permitted.filter(_.colour == White))

  def castlingMust(p: Castling => Result) = prop {(ca: Set[CastlingAvailability]) => p(Castling(ca))}

  def emptyFlagIsDash = Castling(permitted = Set()).flag must_== "-"
}
