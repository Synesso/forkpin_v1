package forkpin

import org.openqa.selenium.remote.{DesiredCapabilities, RemoteWebDriver}
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
      new RemoteWebDriver(new URL(properties("REMOTE_WEBDRIVER_URL")), capabilities)
    }

    val driver = properties.get("WEB_DRIVER") match {
      case Some("remote") => remoteDriver
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
