package forkpin.persist

import scala.slick.driver.JdbcProfile
import java.sql.Timestamp
import org.slf4j.LoggerFactory

trait Profile {
  val profile: JdbcProfile
  def now = new Timestamp(System.currentTimeMillis)
  lazy val logger =  LoggerFactory.getLogger(getClass)
}
