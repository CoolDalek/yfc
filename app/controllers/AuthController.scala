package controllers

import controllers.filters.{ControllerUtils, CustomRequest}
import javax.inject.{Inject, Singleton}
import models.dto.{SignInDTO, UserDTO}
import monix.execution.Scheduler
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.AuthService

import scala.concurrent.ExecutionContext

@Singleton
class AuthController @Inject()(cc: ControllerComponents, authService: AuthService)
                              (implicit ex: ExecutionContext, sc: Scheduler) extends ControllerUtils(cc) {

  def signIn: Action[AnyContent] = actionWithForm(SignInDTO.form) { request =>
    authService.signIn(request.parsedBody).map(userId =>
      Ok.withSession(CustomRequest.CookieUserId -> userId.toString)
    )
  }

  def signOut: Action[AnyContent] = Action.apply {
    Ok.withNewSession
  }

  def signUp: Action[AnyContent] = actionWithForm(UserDTO.form) {
    request => authService.signUp(request.parsedBody).map(_ => Ok)
  }

  def confirm(token: String): Action[AnyContent] = simpleAction { implicit request =>
    authService.confirm(token).map(_ => Ok)
  }

}
