package forkpin

import java.io.File
import scala.io.Source

trait Config {

  val executingDirectly = new File(".env").isFile
  val properties = if (executingDirectly) envFile else sys.env

  lazy val envFile = Source.fromFile(".env").getLines().map(_.split("=")).map{line =>
    (line(0), line.tail.mkString("="))
  }.toMap

}
