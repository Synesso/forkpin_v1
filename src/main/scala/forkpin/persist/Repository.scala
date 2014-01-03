package forkpin.persist

import scala.slick.jdbc.JdbcBackend.Database
import scala.slick.driver.JdbcProfile
import forkpin.web.gplus.PeopleService
import forkpin.{Challenge, Game}
import org.slf4j.LoggerFactory

// todo - future return values throughout this level and the *components below
class Repository(db: Database, jdbcProfile: JdbcProfile) {
  lazy val logger =  LoggerFactory.getLogger(getClass)

  def challenge(id: Int, key: String): Option[Challenge] = {
    db withSession { implicit s =>
      for {
        c <- dataAccess.challenge(id)
        u <- dataAccess.user(c.challengerId)
      } yield Challenge(c.id.get, u, c.email, c.key, c.created)
    } match {
      case result @ Some(Challenge(_, _, _, k, _)) if k == key => result
      case _ => None
    }
  }

  def createChallenge(challenger: User, email: String): Challenge = {
    val uuid = java.util.UUID.randomUUID().toString
    val row = db withSession { implicit s =>
      dataAccess.insert(ChallengeRow(None, challenger.gPlusId, email, uuid, dataAccess.now))
    }
    Challenge(row.id.get, challenger, email, uuid, row.created)
  }

  // todo - don't let someone accept a challenge if they created it.
  def acceptChallenge(challenged: User, challengeId: Int, key: String): Either[ChallengeAcceptFailure, Game] = {
    logger.info(s"User ${challenged.displayName} accepted challenge $challengeId")
    db withSession { implicit s =>
      dataAccess.challenge(challengeId).map{ challenge =>
        if (challenge.key == key) {
          logger.info("key matched, deleting challenge and creating game")
          val gameRow = dataAccess insert GameRow(None, challenged.gPlusId, challenge.challengerId)
          dataAccess.delete(challenge)
          Right(game(gameRow.id.get).get)
        } else {
          logger.info("key did not match")
          Left(ChallengeAcceptFailure("provided key did not match"))
        }
      }.getOrElse(Left(ChallengeAcceptFailure(s"challenge $challengeId does not exist")))
    }
  }

  def game(id: Int): Option[Game] = db withSession { implicit s =>
    for {
      g <- dataAccess.game(id)
      w <- dataAccess.user(g.whiteId)
      b <- dataAccess.user(g.blackId)
    } yield Game(id, w, b) afterMoves g.moves
 }

  def games(user: User): Seq[Game] = db withSession { implicit s =>
    for {
      g <- dataAccess.games(user)
      w <- if (user.gPlusId == g.whiteId) Some(user) else dataAccess.user(g.whiteId)
      b <- if (user.gPlusId == g.blackId) Some(user) else dataAccess.user(g.blackId)
    } yield Game(g.id.get, w, b) afterMoves g.moves
  }

  def update(game: Game) = {
    val gameRow = GameRow(Some(game.id), game.white.gPlusId, game.black.gPlusId,
      game.moves.map(m => s"${m.from}${m.to}").mkString)
    db withSession { implicit s => dataAccess.update(gameRow) }
  }

  def user(id: String) = db withSession { implicit s => dataAccess.user(id) }

  def userOrBuild(id: String, peopleService: PeopleService) = db withSession { implicit s =>
    val user = dataAccess.user(id)
    user.getOrElse{
      // todo - memoize the peopleService result
      val person = peopleService.get(id)
      val user = User(id, person.getDisplayName, dataAccess.now)
      dataAccess.insert(user)
      user
    }
  }

  def recreate() = {
    class Builder(override val profile: JdbcProfile) extends UserComponent with GameComponent with ChallengeComponent with Profile {
      def build() = {
        db withSession { implicit s =>
          dropChallengesTable
          dropGamesTable
          dropUsersTable
          createUsersTable
          createGamesTable
          createChallengesTable
        }
      }
    }
    new Builder(jdbcProfile).build()
  }

  val dataAccess = new UserComponent with GameComponent with ChallengeComponent with Profile {
    val profile: JdbcProfile = jdbcProfile
  }
}

case class ChallengeAcceptFailure(reason: String)
