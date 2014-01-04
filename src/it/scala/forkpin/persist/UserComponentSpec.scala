package forkpin.persist

import org.specs2.Specification
import java.sql.Timestamp
import scala.slick.driver.{JdbcProfile, H2Driver}
import scala.slick.jdbc.JdbcBackend.Database

class UserComponentSpec extends Specification { def is = s2"""

  Must be able to insert and retrieve users $saveAndLoad

"""

  class UserRepo(override val profile: JdbcProfile) extends UserComponent with Profile
  val userRepo = new UserRepo(H2Driver)
  val database = Database.forURL("jdbc:h2:mem:tests", driver = "org.h2.Driver")

  def saveAndLoad = {
    val expected = User("123", "Bob", new Timestamp(System.currentTimeMillis))
    val actual = database withSession { implicit s =>
      userRepo.insert(expected)
      userRepo.user("123")
    }
    actual must_== expected
  }

}

// todo - is the difference around how the userRepo is constructed, compared to DAL?