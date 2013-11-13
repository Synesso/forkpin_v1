package forkpin.web.gplus

import com.google.api.client.googleapis.auth.oauth2.{GoogleTokenResponse, GoogleCredential}
import com.google.api.services.plus.Plus
import forkpin.Config
import com.google.api.services.plus.model.Person

class PeopleService(token: String) extends GPlusOperations with Config {

  def get(id: String): Person = {
    val credential = new GoogleCredential.Builder().setJsonFactory(jsonFactory).setTransport(transport)
      .setClientSecrets(clientId, clientSecret).build.setFromTokenResponse(
      jsonFactory.fromString(token, classOf[GoogleTokenResponse]))
    val plusService = new Plus.Builder(transport, jsonFactory, credential).setApplicationName(appName).build
    plusService.people.get(id).execute
  }

}
