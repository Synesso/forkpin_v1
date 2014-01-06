package forkpin.persist

import org.specs2.Specification
import java.sql.Timestamp


class GameComponentSpec extends Specification { def is = s2"""

  Must be able to insert and retrieve games ${GameRepo().saveAndLoad}
  Must be able to loadGamesForUser ${GameRepo().loadGamesForUser}
  Must be able to update a game ${GameRepo().updateGame}

"""

  case class GameRepo() extends TestDatabase {
    def saveAndLoad = {
      repo.insert(white)
      repo.insert(black)
      val expected = GameRow(None, white.gPlusId, black.gPlusId)
      val actual = repo.insert(expected)
      actual must_== expected.copy(id = Some(1))
    }

    def loadGamesForUser = {
      val whiteGame = GameRow(None, white.gPlusId, black.gPlusId)
      val blackGame = GameRow(None, black.gPlusId, white.gPlusId)
      val notWhiteGame = GameRow(None, black.gPlusId, black.gPlusId)
      repo.insert(white)
      repo.insert(black)
      val game1 = repo.insert(whiteGame)
      val game2 = repo.insert(blackGame)
      repo.insert(notWhiteGame)
      val actual = repo.games(white)
      actual must containTheSameElementsAs(Seq(game1, game2))
    }

    def updateGame = {
      repo.insert(white)
      val expected = repo.insert(GameRow(None, white.gPlusId, white.gPlusId)).copy(moves = "d2d4d7d5")
      repo.update(expected)
      val actual = repo.game(expected.id.get)
      Some(expected) must_== actual
    }
  }
}
