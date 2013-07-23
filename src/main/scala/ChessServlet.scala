import com.google.api.client.googleapis.auth.oauth2.{GoogleTokenResponse, GoogleCredential}
import com.google.api.services.plus.Plus
import gplus.{Token, GPlusOperations}
import java.math.BigInteger
import java.security.SecureRandom
import org.scalatra._
import org.slf4j.LoggerFactory
import scalate.ScalateSupport
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._

class ChessServlet extends ScalatraServlet with ScalateSupport with JacksonJsonSupport with GPlusOperations {
  protected implicit val jsonFormats: Formats = DefaultFormats

  val logger =  LoggerFactory.getLogger(getClass)
  val appName = sys.env.getOrElse("APPLICATION_NAME", "unknown_application_name")
  val clientId = sys.env.getOrElse("CLIENT_ID", "unknown_client_id")
  val clientSecret = sys.env.getOrElse("CLIENT_SECRET", "unknown_client_secret")

  get("/") {
    contentType="text/html"
    val state = new BigInteger(130, new SecureRandom).toString(32)
    session.setAttribute("state", state)
    jade("index",
      "state" -> state,
      "clientId" -> clientId,
      "pageTitle" -> "forkp.in",
      "appName" -> appName)
  }

  post("/connect") {
    authorisedJsonResponse(_ => Ok(reason = "Current user is already connected"),
      if (!params.get("state").equals(session.get("state"))) Unauthorized(reason = "Invalid state parameter")
      else {
        session.removeAttribute("state")
        tokenInfoFor(request).fold({
          exception => InternalServerError(reason = s"Failed to read token data from Google: ${exception.getMessage}")
        }, {
          case (tokenResponse, tokenInfo) =>
            if (tokenInfo.containsKey("error")) Unauthorized(reason = tokenInfo.get("error").toString)
            else if (!request.getParameter("gplus_id").equals(tokenInfo.getUserId)) Unauthorized(reason = "Token's user ID doesn't match given user ID")
            else if (!clientId.equals(tokenInfo.getIssuedTo)) Unauthorized(reason = "Token's client ID does not match app's")
            else {
              session.setAttribute("token", tokenResponse)
              Ok(reason = "Successfully connected user")
            }
        })
      }
    )
  }

  post("/disconnect") {
    authorisedJsonResponse {token =>
      revoke(token, request).fold({
        exception => InternalServerError(reason = s"Failed to read token data from Google: ${exception.getMessage}")
      }, {
        response =>
          session.remove("token")
          Ok(reason = "Successfully disconnected user")
      })
    }
  }

  get("/people") {
    authorisedJsonResponse {token =>
      val credential = new GoogleCredential.Builder().setJsonFactory(jsonFactory).setTransport(transport)
        .setClientSecrets(clientId, clientSecret).build.setFromTokenResponse(
        jsonFactory.fromString(token.value, classOf[GoogleTokenResponse]))
      val plusService = new Plus.Builder(transport, jsonFactory, credential).setApplicationName(appName).build
      Ok(s"${plusService.people.list("me", "visible").execute}")
    }
  }

  notFound {
    findTemplate(requestPath) map { path =>
      contentType = "text/html"
      layoutTemplate(path)
    } orElse serveStaticResource() getOrElse resourceNotFound()
  }

  after() {
    val queryString = if (request.getQueryString == null) "" else s"${request.getQueryString} "
    logger.info(s"${request.getMethod} ${request.getRequestURI} $queryString=> ${response.status}")
  }

  private def token = session.get("token")

  private def authorisedJsonResponse(result: (Token) => ActionResult): ActionResult = {
    authorisedJsonResponse(result, Unauthorized("Current user is not connected."))
  }

  private def authorisedJsonResponse(result: (Token) => ActionResult, orElse: => ActionResult): ActionResult = {
    jsonResponse(token.map{t => result(Token(s"$t"))}.getOrElse(orElse))
  }

  private def jsonResponse(result: => ActionResult) = {
    contentType = formats("json")
    result
  }

}
