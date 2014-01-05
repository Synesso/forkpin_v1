package forkpin.persist


class GameComponentSpec extends ComponentSpecification { def is = s2"""

  Must be able to insert and retrieve games $saveAndLoad

"""

  def saveAndLoad = {
    val expected = GameRow(None, "w_id", "b_id")
    repo.insert(User("w_id", "white", repo.now))
    repo.insert(User("b_id", "black", repo.now))
    val actual = repo.insert(expected)
    actual must_== expected.copy(id = Some(1))
  }

}
