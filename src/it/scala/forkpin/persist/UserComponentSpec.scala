package forkpin.persist

import java.sql.Timestamp

class UserComponentSpec extends ComponentSpecification { def is = s2"""

  Must be able to insert and retrieve users $saveAndLoad

"""

  def saveAndLoad = {
    val expected = User("123", "Bob", new Timestamp(System.currentTimeMillis))
    repo.insert(expected)
    val actual = repo.user("123")
    actual must_== Some(expected)
  }

}