package forkpin.web

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{ServletHolder, DefaultServlet, ServletContextHandler}
import forkpin.Config
import eshq.EventSourceServlet

object JettyLauncher {
  def main(args: Array[String]) {
    new JettyLauncher().start
  }
}

class JettyLauncher extends Config {

  private val server = new Server(port)

  def start = {
    val context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS)

    context.addServlet(classOf[EventSourceServlet], "/eshq/*")
    context.addServlet(classOf[ChessServlet], "/*")
    val defaultServlet = new ServletHolder(classOf[DefaultServlet])
    defaultServlet.setName("default")
    context.addServlet(defaultServlet, "/")
    context.setResourceBase("src/main/webapp")

    if (properties.contains("DATABASE_FORCE_CREATE")) repository.recreate()

    server.start()
    server.join()

    this
  }

  def stop = {
    server.stop()
    this
  }

}