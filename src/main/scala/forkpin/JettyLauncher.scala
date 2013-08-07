package forkpin

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{ServletHolder, DefaultServlet, ServletContextHandler}

object JettyLauncher {
  def main(args: Array[String]) {
    val port = sys.env.get("PORT").map(_.toInt).getOrElse(5000)
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

}