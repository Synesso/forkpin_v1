package forkpin

import java.sql.Timestamp
import org.slf4j.LoggerFactory
import scala.slick.driver.PostgresDriver.simple._
import Database.threadLocalSession
import scala.slick.jdbc.meta.MTable
import scala.slick.lifted.DDL

object Persistent extends Config {

  val logger =  LoggerFactory.getLogger(getClass)

  val tables = Seq(Games, Users, Challenges)

  case class Challenge(id: Option[Int], challengerId: String, challengedId: Option[String], created: Timestamp)
  case class User(gPlusId: String, firstSeen: Timestamp)
  case class GameRow(id: Option[Int], whiteId: String, blackId: String, moves: String = "")

  object Users extends Table[User]("users") {
    def gPlusId = column[String]("gplus_id", O.PrimaryKey)
    def firstSeen = column[Timestamp]("first_login")
    def * = gPlusId ~ firstSeen <> (User, User.unapply _)
  }

  object Games extends Table[GameRow]("games") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def whiteId = column[String]("white_id")
    def blackId = column[String]("black_id")
    def moves = column[String]("moves")
    def * = id.? ~ whiteId ~ blackId ~ moves <> (GameRow, GameRow.unapply _)
    def white = foreignKey("white_fk", whiteId, Users)(_.gPlusId)
    def black = foreignKey("black_fk", blackId, Users)(_.gPlusId)
    def forInsert = whiteId ~ blackId ~ moves <> (
      {t => GameRow(None, t._1, t._2, t._3)},
      {g: GameRow => Some((g.whiteId, g.blackId, g.moves))})
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

  val database: Database = {
    val driver = properties("DATABASE_DRIVER")
    val url = properties("DATABASE_JDBC_URL")
    val user = properties("DATABASE_USERNAME")
    val password = properties("DATABASE_USERPWD")
    Database.forURL(url, driver = driver, user = user, password = password)
  }

  def create() = database withSession {
    if (properties.contains("DATABASE_FORCE_CREATE")) {
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

  def user(gPlusId: String) = database withSession {
    val userQuery = Query(Users).filter(_.gPlusId === gPlusId)
    userQuery.firstOption.getOrElse{
      val u = User(gPlusId, now)
      Users.insert(u)
      u
    }
  }

  def createChallenge(user: User): Either[Challenge, Game] = database withSession {
    logger.info(s"Received open challenge from forkpin.User(${user.gPlusId})")
    val challengeQuery = Query(Challenges).filter(_.challengerId =!= user.gPlusId)
    challengeQuery.firstOption.map{c =>
      Query(Challenges).filter(_.id === c.id).delete
      val gameRow = Games.forInsert returning Games insert GameRow(None, user.gPlusId, c.challengerId)
      Right(Game.buildFrom(gameRow)) // todo - randomise white/black
    }.getOrElse{
      Left(Challenges.forInsert returning Challenges insert Challenge(None, user.gPlusId, None, now))
    }
  }

  def game(id: Int): Option[Game] = database withSession {
    val gameRow = Query(Games).filter(_.id === id).firstOption
    gameRow.map(Game.buildFrom)
  }


  def now = new Timestamp(System.currentTimeMillis)

}
