package forkpin

import java.io.File
import scala.io.Source
import org.slf4j.LoggerFactory

trait Config {

  val logger =  LoggerFactory.getLogger(getClass)
  val executingDirectly = new File(".env").exists
  val properties = if (executingDirectly) envFile else sys.env

  lazy val envFile = Source.fromFile(".env").getLines().map(_.split("=")).map{line =>
    (line(0), line.tail.mkString("="))
  }.toMap

}
