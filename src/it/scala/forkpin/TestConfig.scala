package forkpin

import org.openqa.selenium.remote.{HttpCommandExecutor, DesiredCapabilities, RemoteWebDriver}
import org.openqa.selenium.firefox.FirefoxDriver
import java.net.URL
import java.util.concurrent.TimeUnit._

trait TestConfig extends Config {

  lazy val webDriver = {

    lazy val firefoxDriver = {
      val capabilities = DesiredCapabilities.firefox
      capabilities.setJavascriptEnabled(true)
      new FirefoxDriver
    }

    lazy val remoteDriver = {
      val capabilities = DesiredCapabilities.firefox
      capabilities.setJavascriptEnabled(true)
      capabilities.setCapability("record-video", true)
      capabilities.setCapability("build", sys.env("TRAVIS_BUILD_NUMBER"))
      capabilities.setCapability("tunnel-identifier", sys.env("TRAVIS_JOB_NUMBER"))
      val username = sys.env("SAUCE_USERNAME")
      val accessKey = sys.env("SAUCE_ACCESS_KEY")
      val hubURL = new URL(s"http://$username:$accessKey@localhost:4445/wd/hub")
      val commandExecutor = new HttpCommandExecutor(hubURL)
      new RemoteWebDriver(commandExecutor, capabilities)
    }

    val driver = sys.env.get("TRAVIS") match {
      case Some("true") => remoteDriver
      case _ => firefoxDriver
    }

    driver.manage.timeouts.implicitlyWait(5, SECONDS)
    driver
  }

  val hsqldbConfig = Map(
    "DATABASE_DRIVER" -> "org.h2.Driver",
    "DATABASE_JDBC_URL" -> "jdbc:h2:mem:tests",
    "DATABASE_USERNAME" -> "",
    "DATABASE_USERPWD" -> "",
    "DATABASE_FORCE_CREATE" -> "true",
    "DATABASE_PROFILE" -> "H2"
  )

  override lazy val properties = ConfigMemo.properties ++ hsqldbConfig

}
