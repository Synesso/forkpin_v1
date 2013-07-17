import com.google.api.client.auth.oauth2.TokenResponse
import com.google.api.client.googleapis.auth.oauth2.{GoogleTokenResponse, GoogleCredential, GoogleAuthorizationCodeTokenRequest}
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.oauth2.Oauth2
import com.google.api.services.oauth2.model.Tokeninfo
import com.google.api.services.plus.Plus
import java.io.IOException
import java.math.BigInteger
import java.security.SecureRandom
import javax.servlet.http.HttpServletRequest
import org.scalatra._
import org.slf4j.LoggerFactory
import scalate.ScalateSupport
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._

class ChessServlet extends ScalatraServlet with ScalateSupport with JacksonJsonSupport {
  protected implicit val jsonFormats: Formats = DefaultFormats

  val logger =  LoggerFactory.getLogger(getClass)
  val appName = sys.env.getOrElse("APPLICATION_NAME", "unknown_application_name")
  val clientId = sys.env.getOrElse("CLIENT_ID", "unknown_client_id")
  val clientSecret = sys.env.getOrElse("CLIENT_SECRET", "unknown_client_secret")
  val transport = new NetHttpTransport
  val jsonFactory = new JacksonFactory

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
    contentType = formats("json")

    val result: ActionResult = {
      if (session.get("token").isDefined) Ok(reason = "Current user is already connected.")
      else if (!params.get("state").equals(session.get("state"))) Unauthorized(reason = "Invalid state parameter.")
      else {
        session.removeAttribute("state")
        tokenInfoFor(request).fold({
          exception => InternalServerError(reason = "Failed to read token data from Google. %s".format(exception.getMessage))
        }, {
          case (tokenResponse, tokenInfo) =>
            if (tokenInfo.containsKey("error")) Unauthorized(reason = tokenInfo.get("error").toString)
            else if (!request.getParameter("gplus_id").equals(tokenInfo.getUserId)) Unauthorized(reason = "Token's user ID doesn't match given user ID.")
            else if (!clientId.equals(tokenInfo.getIssuedTo)) Unauthorized(reason = "Token's client ID does not match app's.")
            else {
              session.setAttribute("token", tokenResponse)
              Ok(reason = "Successfully connected user.")
            }
        })
      }
    }
    logger.info("Returning %s".format(result))
    result
  }

  get("/people") {
    contentType = formats("json")
    val (code, message): (Int, String) = {
      session.get("token").map{tokenData =>
        val credential = new GoogleCredential.Builder().setJsonFactory(jsonFactory).setTransport(transport)
          .setClientSecrets(clientId, clientSecret).build.setFromTokenResponse(
          jsonFactory.fromString(String.valueOf(tokenData), classOf[GoogleTokenResponse]))
        val plusService = new Plus.Builder(transport, jsonFactory, credential).setApplicationName(appName).build
        val people = plusService.people.list("me", "visible").execute
        (200, String.valueOf(people))
      }.getOrElse{(401, "Current user is not connected.")}
    }
    status_=(code)
    message
  }

  def tokenInfoFor(request: HttpServletRequest): Either[IOException, (TokenResponse, Tokeninfo)] = try {
    val tokenResponse = new GoogleAuthorizationCodeTokenRequest(transport, jsonFactory, clientId, clientSecret,
      request.body, "postmessage").execute
    val credential = new GoogleCredential.Builder().setJsonFactory(jsonFactory).setTransport(transport)
      .setClientSecrets(clientId, clientSecret).build.setFromTokenResponse(tokenResponse)
    val oath2 = new Oauth2.Builder(transport, jsonFactory, credential).setApplicationName(appName).build
    Right((tokenResponse, oath2.tokeninfo.setAccessToken(credential.getAccessToken).execute))
  } catch {
    case i: IOException => Left(i)
  }

  notFound {
    findTemplate(requestPath) map { path =>
      contentType = "text/html"
      layoutTemplate(path)
    } orElse serveStaticResource() getOrElse resourceNotFound()
  }
}
