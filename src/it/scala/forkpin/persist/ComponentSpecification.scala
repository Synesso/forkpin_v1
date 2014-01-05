package forkpin.persist

import forkpin.TestConfig
import org.specs2.Specification
import scala.slick.driver.JdbcProfile

trait ComponentSpecification extends Specification with TestConfig {

  class Repo(override val profile: JdbcProfile) extends ChallengeComponent with GameComponent with UserComponent with Profile
  val repo = new Repo(jdbcProfile)
  implicit val session = database withSession { s => s }

  lazy val dbInit = ComponentSpecificationMemo.initDBs({ () =>
    try { repo.createUsersTable } catch { case e: Exception => }
    try { repo.createGamesTable } catch { case e: Exception => }
    try { repo.createChallengesTable } catch { case e: Exception => }
  })
  dbInit

}

object ComponentSpecificationMemo {

  def initDBs(thunk: () => Unit): Unit = thunk()

}