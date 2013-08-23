package forkpin.web.gplus

import java.io.IOException
import com.google.api.client.auth.oauth2.TokenResponse
import com.google.api.services.oauth2.model.Tokeninfo
import com.google.api.client.googleapis.auth.oauth2.{GoogleTokenResponse, GoogleCredential, GoogleAuthorizationCodeTokenRequest}
import com.google.api.services.oauth2.Oauth2
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import org.scalatra.servlet.RichRequest
import com.google.api.client.http.{HttpResponse, GenericUrl}

trait GPlusOperations {
  val appName: String
  val clientId: String
  val clientSecret: String
  val transport = new NetHttpTransport
  val jsonFactory = new JacksonFactory

  def tokenInfoFor(request: RichRequest): Either[IOException, (TokenResponse, Tokeninfo)] = try {
    val tokenResponse = new GoogleAuthorizationCodeTokenRequest(transport, jsonFactory, clientId, clientSecret,
      request.body, "postmessage").execute
    val credential = new GoogleCredential.Builder().setJsonFactory(jsonFactory).setTransport(transport)
      .setClientSecrets(clientId, clientSecret).build.setFromTokenResponse(tokenResponse)
    val oath2 = new Oauth2.Builder(transport, jsonFactory, credential).setApplicationName(appName).build
    Right((tokenResponse, oath2.tokeninfo.setAccessToken(credential.getAccessToken).execute))
  } catch {
    case i: IOException => Left(i)
  }

  def revoke(token: Token, request: RichRequest): Either[IOException, HttpResponse] = try {
    val tokenResponse = jsonFactory.fromString(token.value, classOf[GoogleTokenResponse])
    val credential = new GoogleCredential.Builder().setJsonFactory(jsonFactory).setTransport(transport)
      .setClientSecrets(clientId, clientSecret).build.setFromTokenResponse(tokenResponse)
    val url = new GenericUrl("https://accounts.google.com/o/oauth2/revoke?token=%s".format(credential.getAccessToken))
    val response = transport.createRequestFactory().buildGetRequest(url).execute()
    Right(response)
  } catch {
    case i: IOException => Left(i)
  }
}

case class Token(value: String)
