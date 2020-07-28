package services.impl

import javax.inject.{Inject, Singleton}
import monix.eval.Task
import play.api.Configuration
import play.api.libs.mailer.{Email, MailerClient}
import services.MailerService

@Singleton
class MailerServiceImpl @Inject()(mailerClient: MailerClient, config: Configuration) extends MailerService {

  override def sendMail(address: String, token: String): Task[Unit] = {
    val email = Email(
      subject = "Email confirmation",
      from = s"<${config.get[String]("play.mailer.user")}>",
      to = Seq(s"<$address>"),
      bodyText = Some(s"<h3>Click <a href='${config.get[String]("hostname")}/confirmation-message/?token=$token'>here</a> to confirm your email</h3>")
    )
    Task(mailerClient send email)
  }

}
