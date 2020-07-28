package services

import monix.eval.Task

trait MailerService {

  def sendMail(address: String, token: String): Task[Unit]

}
