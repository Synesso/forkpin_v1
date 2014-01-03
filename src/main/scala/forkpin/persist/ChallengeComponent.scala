package forkpin.persist

import java.sql.Timestamp
import scala.slick.lifted.Tag
import scala.slick.jdbc.StaticQuery

case class ChallengeRow(id: Option[Int], challengerId: String, email: String, key: String, created: Timestamp)

trait ChallengeComponent { this: Profile with UserComponent with GameComponent =>
  import profile.simple._

  class Challenges(tag: Tag) extends Table[ChallengeRow](tag, "challenges") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def challengerId = column[String]("challenger_id")
    def email = column[String]("email")
    def key = column[String]("key")
    def created = column[Timestamp]("created")
    def * = (id.?, challengerId, email, key, created) <> (ChallengeRow.tupled, ChallengeRow.unapply)
    def challenger = foreignKey("challenger_fk", challengerId, users)(_.gPlusId)
  }

  val challenges = TableQuery[Challenges]

  def challenge(challengeId: Int)(implicit session: Session): Option[ChallengeRow] = {
    challenges.filter(_.id === challengeId).firstOption
  }

  def delete(challenge: ChallengeRow)(implicit session: Session) = {
    challenges.filter(_.id === challenge.id).delete
  }

  def insert(challenge: ChallengeRow)(implicit session: Session): ChallengeRow = {
    logger.info(s"$challenge created")
    (challenges returning challenges) insert challenge
  }

  def createChallengesTable(implicit session: Session) = challenges.ddl.create
  def dropChallengesTable(implicit session: Session) = StaticQuery.updateNA("drop table challenges cascade").execute
}
