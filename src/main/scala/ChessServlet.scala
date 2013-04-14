import org.scalatra._
import scalate.ScalateSupport

class ChessServlet extends ScalatraServlet with ScalateSupport {

  get("/") {
    contentType="text/html"
    jade("index",
      "pageTitle" -> "Welcome to Jade",
      "headline" -> "Hello my pretties")
  }

  notFound {
    findTemplate(requestPath) map { path =>
      contentType = "text/html"
      layoutTemplate(path)
    } orElse serveStaticResource() getOrElse resourceNotFound()
  }
}