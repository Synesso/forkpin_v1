package forkpin.persist

import scala.slick.driver.{H2Driver, JdbcProfile}
import scala.slick.jdbc.JdbcBackend._
import java.util.UUID
import java.sql.Timestamp

trait TestDatabase {

  class Repo(override val profile: JdbcProfile) extends ChallengeComponent with GameComponent with UserComponent with Profile

  val repo = new Repo(H2Driver)
  val database = Database.forURL(s"jdbc:h2:mem:${UUID.randomUUID}", driver = "org.h2.Driver")
  implicit val session = database withSession {
    s => s
  }
  repo.createUsersTable
  repo.createGamesTable
  repo.createChallengesTable

  val (white, black) = (User("w_id", "white", new Timestamp(System.currentTimeMillis)),
    User("b_id", "black", new Timestamp(System.currentTimeMillis)))
  val now = new Timestamp(System.currentTimeMillis)

}

