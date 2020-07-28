package services

import helpers.{BCryptHelper, TimeHelper, TokenHelper}
import models.dto.{SignInDTO, UserDTO}
import monix.eval.Task

trait AuthService {

  def confirm(tokenBody: String)(implicit th: TimeHelper): Task[Unit]

  def signUp(dto: UserDTO)(implicit timeH: TimeHelper, tokenH: TokenHelper, BCh: BCryptHelper): Task[Unit]

  def signIn(credentials: SignInDTO): Task[Long]

}