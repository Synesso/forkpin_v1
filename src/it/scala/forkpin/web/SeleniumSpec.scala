package forkpin.web

import org.specs2.Specification
import org.specs2.specification.{Step, Fragments}
import forkpin.TestConfig

trait SeleniumSpec extends Specification with TestConfig {

  override def map(fs: => Fragments) = fs ^ Step(close())

  def close() {
    webDriver.quit()
  }

}
