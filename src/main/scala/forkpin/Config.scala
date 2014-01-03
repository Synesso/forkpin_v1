package forkpin

import java.io.File
import scala.io.Source
import org.slf4j.LoggerFactory
import scala.slick.jdbc.JdbcBackend.Database
import scala.slick.driver.{PostgresDriver, H2Driver}
import forkpin.persist.Repository

trait Config {

  lazy val properties = ConfigMemo.properties

  lazy val logger =  LoggerFactory.getLogger(getClass)
  lazy val port = properties.get("PORT").map(_.toInt).getOrElse(5000)
  lazy val appName = properties("APPLICATION_NAME")
  lazy val clientId = properties("CLIENT_ID")
  lazy val clientSecret = properties("CLIENT_SECRET")

  lazy val database = {
    val driver = properties("DATABASE_DRIVER")
    val url = properties("DATABASE_JDBC_URL")
    val user = properties("DATABASE_USERNAME")
    val password = properties("DATABASE_USERPWD")
    Database.forURL(url, driver = driver, user = user, password = password)
  }

  lazy val jdbcProfile = properties.get("DATABASE_PROFILE") match {
    case Some("H2") => H2Driver
    case _ => PostgresDriver
  }

  lazy val repository = new Repository(database, jdbcProfile)

}

// read the properties only once
object ConfigMemo {

  lazy val properties = {
    lazy val envFile = Source.fromFile(".env").getLines().map(_.split("=")).map{line =>
      (line(0), line.tail.mkString("="))
    }.toMap
    val p = if (new File(".env").exists) envFile else sys.env
    println("core properties")
    p.foreach(println)
    p
  }

}