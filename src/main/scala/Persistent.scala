import java.sql.{Timestamp, Date}
import org.slf4j.LoggerFactory
import scala.slick.driver.PostgresDriver.simple._
import Database.threadLocalSession
import scala.slick.jdbc.meta.MTable
import scala.slick.lifted.DDL

object Persistent {

  val logger =  LoggerFactory.getLogger(getClass)

  object Users extends Table[(String, Timestamp, Timestamp)]("users") {
    def gPlusId = column[String]("gplus_id", O.PrimaryKey)
    def firstSeen = column[Timestamp]("first_login")
    def lastSeen = column[Timestamp]("last_login")
    def * = gPlusId ~ firstSeen ~ lastSeen
  }

  object Games extends Table[(String)]("games") {
    def rarr = column[String]("rarr")
    def * = rarr
  }

  val driver = sys.env("DATABASE_DRIVER")
  val url = sys.env("DATABASE_URL")
  val user = sys.env("DATABASE_USERNAME")
  val password = sys.env("DATABASE_USERPWD")
  val database = Database.forURL(url, driver = driver, user = user, password = password)

  def create() = database withSession {
    val requiredTables = Seq(Games, Users)
    if (sys.env.contains("DATABASE_FORCE_CREATE")) {
      logger.info(s"Dropping $requiredTables")
      requiredTables.map(_.ddl).reduceLeft(_ ++ _).drop
    }
    val existingTables = MTable.getTables.list().map(_.name.name)
    val tablesToCreate = requiredTables.filterNot(t => existingTables.contains(t.tableName))
    logger.info(s"Creating $tablesToCreate")
    val ddl: Option[DDL] = tablesToCreate.foldLeft(None: Option[DDL]){(ddl, table) =>
      ddl match {
        case Some(d) => Some(d ++ table.ddl)
        case _ => Some(table.ddl)
      }
    }
    ddl.foreach{_.create}
  }

  def connectedUser(gPlusId: String) = database withSession {
    logger.info(s"Registering $gPlusId")
    val now = new Timestamp(System.currentTimeMillis)
    val q = for { u <- Users if u.gPlusId === gPlusId } yield u.lastSeen
    q.update(now) match {
      case 0 => Users.insert((gPlusId, now, now))
      case _ => Unit
    }
  }

}
