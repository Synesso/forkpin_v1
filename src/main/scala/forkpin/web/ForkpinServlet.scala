package forkpin.web

import org.scalatra.{Unauthorized, ActionResult, ScalatraServlet}
import org.scalatra.json.JacksonJsonSupport
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.scalate.ScalateSupport
import forkpin.persist.Persistent
import Persistent.User
import forkpin.Config
import gplus.Token
import com.github.synesso.eshq.{Secret, Key, EventSourceClient}
import java.net.URL

abstract class ForkpinServlet extends ScalatraServlet with ScalateSupport with JacksonJsonSupport with Config {
  protected implicit val jsonFormats: Formats = DefaultFormats

  notFound {
    findTemplate(requestPath) map { path =>
      contentType = "text/html"
      layoutTemplate(path)
    } orElse serveStaticResource() getOrElse resourceNotFound()
  }

  error { case e =>
    logger.error("Gone south: ", e)
    status_=(500)
    <span>{e.getMessage}</span>
  }

  def token = session.get("token")
  def user = session("user").asInstanceOf[User]

  def authorisedJsonResponse(result: (Token) => ActionResult): ActionResult = {
    authorisedJsonResponse(result, Unauthorized("Current user is not connected."))
  }

  def authorisedJsonResponse(result: (Token) => ActionResult, orElse: => ActionResult): ActionResult = {
    jsonResponse(token.map{t => result(Token(s"$t"))}.getOrElse(orElse))
  }

  def jsonResponse(result: => ActionResult) = {
    contentType = formats("json")
    result
  }

  lazy val eventSourceClient = {
    val (key, secret, serviceURL) = (properties("ESHQ_KEY"), properties("ESHQ_SECRET"), properties("ESHQ_URL"))
    EventSourceClient(Key(key), Secret(secret), new URL(serviceURL))
  }

}
