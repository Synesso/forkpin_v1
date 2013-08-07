package forkpin

import java.sql.Timestamp
import org.slf4j.LoggerFactory
import scala.slick.driver.PostgresDriver.simple._
import Database.threadLocalSession
import scala.slick.jdbc.meta.MTable
import scala.slick.lifted.DDL

object Persistent {

  val logger =  LoggerFactory.getLogger(getClass)

  val tables = Seq(Games, Users, Challenges)

  object Users extends Table[User]("users") {
    def gPlusId = column[String]("gplus_id", O.PrimaryKey)
    def firstSeen = column[Timestamp]("first_login")
    def lastSeen = column[Timestamp]("last_login")
    def * = gPlusId ~ firstSeen ~ lastSeen <> (User, User.unapply _)
  }

  object Games extends Table[GameRow]("games") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def whiteId = column[String]("white_id")
    def blackId = column[String]("black_id")
    def fen = column[String]("fen")
    def * = id.? ~ whiteId ~ blackId ~ fen <> (GameRow, GameRow.unapply _)
    def white = foreignKey("white_fk", whiteId, Users)(_.gPlusId)
    def black = foreignKey("black_fk", blackId, Users)(_.gPlusId)
    def forInsert = whiteId ~ blackId ~ fen <> (
      {t => GameRow(None, t._1, t._2, t._3)},
      {g: GameRow => Some((g.whiteId, g.blackId, g.fen))})
  }

  object Challenges extends Table[Challenge]("challenges") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def challengerId = column[String]("challenger_id")
    def challengedId = column[Option[String]]("challenged_id")
    def created = column[Timestamp]("created")
    def * = id.? ~ challengerId ~ challengedId ~ created <> (Challenge, Challenge.unapply _)
    def challenger = foreignKey("challenger_fk", challengerId, Users)(_.gPlusId)
    def challenged = foreignKey("challenged_fk", challengedId, Users)(_.gPlusId)
    def forInsert = challengerId ~ challengedId ~ created <> (
      {t => Challenge(None, t._1, t._2, now)},
      {c: Challenge => Some((c.challengerId, c.challengedId, now))})
  }

  val driver = sys.env("DATABASE_DRIVER")
  val url = sys.env("DATABASE_JDBC_URL")
  val user = sys.env("DATABASE_USERNAME")
  val password = sys.env("DATABASE_USERPWD")
  val database = Database.forURL(url, driver = driver, user = user, password = password)

  def create() = database withSession {
    if (sys.env.contains("DATABASE_FORCE_CREATE")) {
      import scala.slick.jdbc.{StaticQuery => Q}
      def existingTables = MTable.getTables.list().map(_.name.name)
      tables.filter(t => existingTables.contains(t.tableName)).foreach{t =>
        logger.info(s"Dropping $t")
        Q.updateNA(s"drop table ${t.tableName} cascade").execute
      }
      val tablesToCreate = tables.filterNot(t => existingTables.contains(t.tableName))
      logger.info(s"Creating $tablesToCreate")
      val ddl: Option[DDL] = tablesToCreate.foldLeft(None: Option[DDL]){(ddl, table) =>
        ddl match {
          case Some(d) => Some(d ++ table.ddl)
          case _ => Some(table.ddl)
        }
      }
      ddl.foreach{_.create}
    }
  }

  def connectedUser(gPlusId: String) = database withSession {
    logger.info(s"Registering $gPlusId")
    val userQuery = Query(Users).filter(_.gPlusId === gPlusId)
    userQuery.firstOption.map{u =>
      Users.filter(_.gPlusId === gPlusId).map(_.lastSeen).update(now)
      u.copy(lastSeen = now)
    }.getOrElse{
      val u = User(gPlusId, now, now)
      Users.insert(u)
      u
    }
  }

  def createChallenge(user: User): Either[Challenge, GameRow] = database withSession {
    logger.info(s"Received open challenge from forkpin.User(${user.gPlusId})")
    val challengeQuery = Query(Challenges).filter(_.challengerId =!= user.gPlusId)
    challengeQuery.firstOption.map{c =>
      Query(Challenges).filter(_.id === c.id).delete
      Right(Games.forInsert returning Games insert GameRow(None, user.gPlusId, c.challengerId)) // todo - randomise white/black
    }.getOrElse{
      Left(Challenges.forInsert returning Challenges insert Challenge(None, user.gPlusId, None, now))
    }
  }

  def game(id: Int): Option[GameRow] = database withSession {
    Query(Games).filter(_.id === id).firstOption
  }


  def now = new Timestamp(System.currentTimeMillis)

}
