package forkpin

import java.util.Date
import forkpin.persist.User

case class Challenge(id: Int, challenger: User, email: String, key: String, created: Date) {
  lazy val forClient: Map[String, Any] = Map("id" -> id, "challenger" -> challenger.forClient)
}
