package forkpin.persist

import scala.slick.ast.ColumnOption.DBType
import scala.slick.lifted.Tag
import scala.slick.jdbc.StaticQuery

case class GameRow(id: Option[Int], whiteId: String, blackId: String, moves: String = "")

trait GameComponent { this: Profile with UserComponent =>
  import profile.simple._

  class Games(tag: Tag) extends Table[GameRow](tag, "games") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def whiteId = column[String]("white_id")
    def blackId = column[String]("black_id")
    def moves = column[String]("moves", DBType("text"))
    def * = (id.?, whiteId, blackId, moves) <> (GameRow.tupled, GameRow.unapply)
    def white = foreignKey("white_fk", whiteId, users)(_.gPlusId)
    def black = foreignKey("black_fk", blackId, users)(_.gPlusId)
  }

  val games = TableQuery[Games]

  def games(user: User)(implicit session: Session): Seq[GameRow] = {
    games.filter(g => g.blackId === user.gPlusId || g.whiteId === user.gPlusId).to[Vector]
  }

  def game(id: Int)(implicit session: Session): Option[GameRow] = {
    games.filter(_.id === id).firstOption
  }

  def insert(gameRow: GameRow)(implicit session: Session): GameRow = {
    (games returning games) insert gameRow
  }

  def update(game: GameRow)(implicit session: Session) = {
    val query = for { g <- games if g.id === game.id } yield g.moves
    query.update(game.moves)
  }

  def createGamesTable(implicit session: Session) = games.ddl.create
  def dropGamesTable(implicit session: Session) = StaticQuery.updateNA("drop table games cascade").execute

}


