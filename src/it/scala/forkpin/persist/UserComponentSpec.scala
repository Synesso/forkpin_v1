package forkpin.persist

import forkpin.TestConfig
import org.specs2.Specification
import java.sql.Timestamp

class UserComponentSpec extends Specification with TestConfig { def is = s2"""

  Must be able to insert and retrieve users $saveAndLoad

"""

  val userRepo = new UserComponent with Profile {
    val profile = jdbcProfile
  }

  def saveAndLoad = {
    val expected = User("123", "Bob", new Timestamp(System.currentTimeMillis))
    val actual = database withSession { implicit s =>
      userRepo.insert(expected)
      userRepo.user("123")
    }
    actual must_== expected
  }

}
