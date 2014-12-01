package forkpin

import org.specs2.{ScalaCheck, Specification}
import org.scalacheck.{Arbitrary, Gen}
import org.specs2.execute.Result

class CastlingAvailabilitySpec extends Specification with ScalaCheck { def is = s2"""

  A non-empty castling object must
    have a flag which is alphabetically sorted $alphabeticallySorted
    have the same size as input set            $sameSizeAsInputSet
    contain only ks or qs                      $onlyKorQ
    contain as many Ks as king sides           $asManyKsAsKingSides
    contain as many Qs as queen sides          $asManyQsAsQueenSides
    contain as many uppercase as white         $asManyLowerCaseAsBlack
    contain as many lowercase as black         $asManyUpperCaseAsWhite
    show availability for black                $availabilityForBlack
    show availability for white                $availabilityForWhite

  An empty castling object must
    have a flag of '-' $emptyFlagIsDash

"""

  implicit val caSet = Arbitrary(Gen.nonEmptyBuildableOf[Set[CastlingAvailability], CastlingAvailability](
    Gen.oneOf(Seq(WhiteKingSide, WhiteQueenSide, BlackKingSide, BlackQueenSide))))

  def alphabeticallySorted = castlingMust(c => c.flag.sorted must_== c.flag)

  def sameSizeAsInputSet = castlingMust(c => c.flag.size must_== c.permitted.size)

  def onlyKorQ = castlingMust(c => c.flag.toLowerCase.replaceAll("[k|q]", "") must beEmpty)

  def asManyKsAsKingSides = castlingMust(c => c.flag.toLowerCase.count(_ == 'k') must_== c.permitted.count(_.side == KingSide))

  def asManyQsAsQueenSides = castlingMust(c => c.flag.toLowerCase.count(_ == 'q') must_== c.permitted.count(_.side == QueenSide))

  def asManyUpperCaseAsWhite = castlingMust(c => c.flag.count(_.isLower) == c.permitted.count(_.colour == Black))

  def asManyLowerCaseAsBlack =    castlingMust(c => c.flag.count(_.isUpper) == c.permitted.count(_.colour == White))

  def availabilityForBlack = castlingMust(c => c.availabilityFor(Black) must_== c.permitted.filter(_.colour == Black))

  def availabilityForWhite = castlingMust(c => c.availabilityFor(White) must_== c.permitted.filter(_.colour == White))

  def castlingMust(p: Castling => Result) = prop {(ca: Set[CastlingAvailability]) => p(Castling(ca))}

  def emptyFlagIsDash = Castling(permitted = Set()).flag must_== "-"
}
