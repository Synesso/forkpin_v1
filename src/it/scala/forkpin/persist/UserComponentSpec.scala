package forkpin.persist

import java.sql.Timestamp
import org.specs2.Specification

class UserComponentSpec extends Specification { def is = s2"""

  Must be able to insert and retrieve users ${UserRepo().saveAndLoad}

"""

  case class UserRepo() extends TestDatabase {
    def saveAndLoad = {
      val expected = User("123", "Bob", now)
      repo.insert(expected)
      val actual = repo.user("123")
      actual must_== Some(expected)
    }
  }


}