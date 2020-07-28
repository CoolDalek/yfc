package services

import helpers.TimeHelper
import models.dto.{SignInDTO, UserDTO}
import monix.eval.Task

trait AuthService {

  def confirm(tokenBody: String)(implicit th: TimeHelper): Task[Unit]

  def signUp(dto: UserDTO)(implicit th: TimeHelper): Task[Unit]

  def signIn(credentials: SignInDTO): Task[Long]

}