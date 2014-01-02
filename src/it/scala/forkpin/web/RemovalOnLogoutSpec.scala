package forkpin.web

import org.specs2.specification.Before

class RemovalOnLogoutSpec extends SeleniumSpec { def is = s2"""

  When a user logs out, all games should disappear ${LoggedOutFromSingleGame().noGamesDisplayed}
  When a user logs out, all user avatars should disappear ${LoggedOutFromSingleGame().noAvatarsDisplayed}

"""

  case class LoggedOutFromSingleGame() extends Before {

    def before = {
      // todo - inject mock DB with appropriate data
      // todo - startup the server with mock DB
      // todo - log in as user with single game
      ???
    }

    def noGamesDisplayed = pending

    def noAvatarsDisplayed = pending

  }

}
