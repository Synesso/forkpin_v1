package forkpin.persist

import java.sql.Timestamp
import scala.slick.lifted.Tag
import scala.slick.jdbc.StaticQuery

case class User(gPlusId: String, displayName: String, firstSeen: Timestamp) {
  lazy val profilePictureUrl = s"https://www.google.com/s2/photos/profile/$gPlusId?sz=50"
  lazy val forClient: Map[String, Any] = Map("gPlusId" -> gPlusId, "displayName" -> displayName,
    "profilePictureUrl" -> profilePictureUrl)
}

trait UserComponent { this: Profile =>
  import profile.simple._

  class Users(tag: Tag) extends Table[User](tag, "users") {
    def gPlusId = column[String]("gplus_id", O.PrimaryKey, O.NotNull)
    def displayName = column[String]("display_name", O.NotNull)
    def firstSeen = column[Timestamp]("first_login", O.NotNull)
    def * = (gPlusId, displayName, firstSeen) <> (User.tupled, User.unapply)
  }
  val users = TableQuery[Users]

  def user(gPlusId: String)(implicit session: Session): Option[User] = {
    users.filter(_.gPlusId === gPlusId).firstOption
  }

  def insert(user: User)(implicit session: Session): Unit = users.insert(user)

  def createUsersTable(implicit session: Session) = users.ddl.create

  def dropUsersTable(implicit session: Session) = StaticQuery.updateNA("drop table users cascade").execute

}