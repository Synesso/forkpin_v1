package forkpin.persist

import org.specs2.Specification


class ChallengeComponentSpec extends Specification { def is = s2"""

  Must be able to insert and retrieve challenges ${ChallengeRepo().saveAndLoad}
  Must be able to delete challenges ${ChallengeRepo().delete}

"""

  case class ChallengeRepo() extends TestDatabase {

    def saveAndLoad = {
      repo.insert(white)
      val input = ChallengeRow(None, white.gPlusId, "some@email", "some_key", now)
      repo.insert(input)
      val actual = repo.challenge(1)
      actual must_== Some(input.copy(id = Some(1)))
    }

    def delete = {
      repo.insert(white)
      val input = ChallengeRow(None, white.gPlusId, "some@email", "some_key", now)
      val inserted = repo.insert(input)
      repo.challenge(inserted.id.get) must beSome[ChallengeRow]
      repo.delete(inserted)
      repo.challenge(inserted.id.get) must beNone
    }

  }

}