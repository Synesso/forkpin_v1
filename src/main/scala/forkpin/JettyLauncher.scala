package forkpin

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{ServletHolder, DefaultServlet, ServletContextHandler}
import scala.sys.SystemProperties

object JettyLauncher {

  def main(args: Array[String]) {

    if (executingDirectly) {
      loadEnv()
    }

    val port = systemProperties.get("PORT").map(_.toInt).getOrElse(5000)
    val server = new Server(port)
    val context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS)

    context.addServlet(classOf[ChessServlet], "/*")
    val defaultServlet = new ServletHolder(classOf[DefaultServlet])
    defaultServlet.setName("default")
    context.addServlet(defaultServlet, "/")
    context.setResourceBase("src/main/webapp")

    server.start()
    server.join()
  }

  private val systemProperties = new SystemProperties
  private val executingDirectly = !systemProperties.contains("APPLICATION_NAME")

  def loadEnv() = {
    io.Source.fromFile(".env").getLines().map(_.split("=")).foreach{line =>
      systemProperties.update(line(0), line.tail.mkString("="))
    }
  }
}