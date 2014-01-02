package forkpin

import courier._, Defaults._
import javax.mail.internet.InternetAddress
import scala.language.reflectiveCalls

object SendMail extends Config {
  private val mailer = Mailer("smtp.gmail.com", 587)
    .auth(a = true)
    .as(properties("SMTP_FROM"), properties("SMTP_PWD"))
    .startTtls(s = true)()

  private def toAddress(challenge: Challenge) = {
    new InternetAddress(properties.getOrElse("SMTP_TO_OVERRIDE", challenge.email))
  }

  def send(c: Challenge, baseUrl: String) = {
    val ftrSend = mailer(Envelope.from("jem.mawson" at "gmail.com")
      .to(toAddress(c))
      .subject(s"${c.challenger.displayName} wants to play chess with you")
      .content(Text(s"""Accept this challenge by clicking this link:
       $baseUrl?challenge=${c.id}&key=${c.key}""")))
    ftrSend.onSuccess {case _ => logger.info(s"email sent to ${c.email}.")}
    ftrSend.onFailure{case t => logger.error(s"email send to ${c.email} failed.", t)}
  }
}
