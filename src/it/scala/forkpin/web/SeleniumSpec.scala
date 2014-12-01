package forkpin.web

import org.specs2.Specification
import org.specs2.specification._
import forkpin.TestConfig

trait SeleniumSpec extends Specification with TestConfig with AfterAll {

  def afterAll {
    webDriver.quit()
  }

}
