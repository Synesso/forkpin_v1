package forkpin.persist

import java.sql.Timestamp
import scala.slick.driver.PostgresDriver.simple._
import Database.threadLocalSession
import scala.slick.jdbc.meta.MTable
import scala.slick.lifted.DDL
import forkpin.{Game, Config}
import scala.slick.lifted.ColumnOption.DBType
import forkpin.web.gplus.PeopleService

object Persistent extends Config {

  val tables = Seq(Games, Users, Challenges)

  case class Challenge(id: Option[Int], challengerId: String, email: String, key: String, created: Timestamp) {
    lazy val challenger = user(challengerId).get
    lazy val forClient: Map[String, Any] = id.map(i =>
      Map("id" -> i, "challenger" -> challenger.forClient)
    ).getOrElse(Map.empty)
  }
  case class User(gPlusId: String, displayName: String, firstSeen: Timestamp) {
    lazy val profilePictureUrl = s"https://www.google.com/s2/photos/profile/$gPlusId?sz=50"
    lazy val forClient: Map[String, Any] = Map("gPlusId" -> gPlusId, "displayName" -> displayName,
      "profilePictureUrl" -> profilePictureUrl)
  }
  case class GameRow(id: Option[Int], whiteId: String, blackId: String, moves: String = "")
  case class ChallengeAcceptFailure(reason: String)
  object GameRow {
    def buildFrom(game: Game) = GameRow(Some(game.id), game.white.gPlusId, game.black.gPlusId,
      game.moves.map(m => s"${m.from}${m.to}").mkString)
  }

  object Users extends Table[User]("users") {
    def gPlusId = column[String]("gplus_id", O.PrimaryKey)
    def displayName = column[String]("display_name")
    def firstSeen = column[Timestamp]("first_login")
    def * = gPlusId ~ displayName ~ firstSeen <> (User, User.unapply _)
  }

  object Games extends Table[GameRow]("games") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def whiteId = column[String]("white_id")
    def blackId = column[String]("black_id")
    def moves = column[String]("moves", DBType("text"))
    def * = id.? ~ whiteId ~ blackId ~ moves <> (GameRow.apply _, GameRow.unapply _)
    def white = foreignKey("white_fk", whiteId, Users)(_.gPlusId)
    def black = foreignKey("black_fk", blackId, Users)(_.gPlusId)
    def forInsert = whiteId ~ blackId ~ moves <> (
      {t => GameRow(None, t._1, t._2, t._3)},
      {g: GameRow => Some((g.whiteId, g.blackId, g.moves))})
  }

  object Challenges extends Table[Challenge]("challenges") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def challengerId = column[String]("challenger_id")
    def email = column[String]("email")
    def key = column[String]("key")
    def created = column[Timestamp]("created")
    def * = id.? ~ challengerId ~ email ~ key ~ created <> (Challenge, Challenge.unapply _)
    def challenger = foreignKey("challenger_fk", challengerId, Users)(_.gPlusId)
    def forInsert = challengerId ~ email ~ key ~ created <> (
      {t => Challenge(None, t._1, t._2, t._3, now)},
      {c: Challenge => Some((c.challengerId, c.email, c.key, now))})
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
    userQuery.firstOption
  }

  // todo - memoize the peopleService result
  def userOrBuild(gPlusId: String, peopleService: PeopleService) = database withSession {
    user(gPlusId).getOrElse{
      val person = peopleService.get(gPlusId)
      val u = User(gPlusId, person.getDisplayName, now)
      Users.insert(u)
      u
    }
  }

  def challenge(challengeId: Int, key: String): Option[Challenge] = database withSession {
    logger.debug(s"Looking for challenge with id $challengeId and key $key")
    Query(Challenges).filter(_.id === challengeId).filter(_.key === key).firstOption
  }

  def createChallenge(challenger: User, email: String): Challenge = database withSession {
    logger.info(s"User ${challenger.gPlusId} challenges player at $email")
    val uuid = java.util.UUID.randomUUID().toString
    Challenges.forInsert returning Challenges insert
      Challenge(None, challenger.gPlusId, email, uuid, now)
  }

  def acceptChallenge(challenged: User, challengeId: Int, key: String): Either[ChallengeAcceptFailure, Game] =
    database withSession {
      // todo - don't let someone accept a challenge if they created it.
    logger.info(s"User ${challenged.displayName} accepted challenge $challengeId")
    Query(Challenges).filter(_.id === challengeId).firstOption.map{c =>
      if (c.key == key) {
        logger.info("key matched, deleting challenge and creating game")
        Query(Challenges).filter(_.id === c.id).delete
        val gameRow = Games.forInsert returning Games insert GameRow(None, challenged.gPlusId, c.challengerId)
        Right(Game.buildFrom(gameRow))
      } else {
        logger.info("key did not match")
        Left(ChallengeAcceptFailure("provided key did not match"))
     }
    }.getOrElse(Left(ChallengeAcceptFailure(s"challenge $challengeId does not exist")))
  }

  def games(user: User): Seq[Game] = database withSession {
    val gameRows = Query(Games).filter{g => g.blackId === user.gPlusId || g.whiteId === user.gPlusId}
    gameRows.to[Vector].map(Game.buildFrom)
  }

  def game(id: Int): Option[Game] = database withSession {
    val gameRow = Query(Games).filter(_.id === id).firstOption
    gameRow.map(Game.buildFrom)
  }

  def updateGame(game: Game) = database withSession {
    val gameRow = GameRow.buildFrom(game)
    val query = for { g <- Games if g.id === game.id } yield g.moves
    query.update(gameRow.moves)
  }

  def now = new Timestamp(System.currentTimeMillis)

}
