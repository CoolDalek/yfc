package controllers.filters

import exceptions.Exceptions._
import monix.eval.Task
import monix.execution.Scheduler
import play.api.data.Form
import play.api.mvc._

import scala.concurrent.ExecutionContext


abstract class ControllerUtils(cc: ControllerComponents)
                              (implicit ex: ExecutionContext, sc: Scheduler) extends AbstractController(cc) {


  def simpleAction(block: Request[AnyContent] => Task[Result]): Action[AnyContent] = Action.async { request =>
    block(request).onErrorRecover(recover).runToFuture
  }

  def simpleAction(asyncResult: Task[Result]): Action[AnyContent] = simpleAction(_ => asyncResult)


  def actionWithForm[A](form: Form[A])(block: CustomRequest[A] => Task[Result]): Action[AnyContent] = simpleAction { implicit request =>
    form.bindFromRequest().fold(
      formWithErrors => Task.raiseError(WrongFormException(formWithErrors.errors)),
      model => block(CustomRequest(None, Some(model), request))
    )
  }

  def userAction(block: CustomRequest[Nothing] => Task[Result]): Action[AnyContent] = simpleAction { request =>
    request.session.get(CustomRequest.CookieUserId) match {
      case Some(id) => block(CustomRequest[Nothing](Some(id.toLong), None, request))
      case _ => Task.raiseError(UnauthorizedException)
    }
  }


  def userAction(res: Task[Result]): Action[AnyContent] = userAction(_ => res)


  def userActionWithForm[A](form: Form[A])(block: CustomRequest[A] => Task[Result]): Action[AnyContent] = userAction { implicit request =>
    form.bindFromRequest()(request.request).fold(
      formWithErrors => Task.raiseError(WrongFormException(formWithErrors.errors)),
      model => block(request.withParsedBody(model))
    )
  }

  private def recover: PartialFunction[Throwable, Result] = {

    //401
    case UnauthorizedException => Unauthorized

    //403
    case ForbiddenException(msg) => Forbidden(msg)
    case UserIsNotActive => Forbidden(UserIsNotActive.getMessage)

    //404
    case NotFoundException(msg) => NotFound(msg)

    //406
    case TokenBrokenOrExpired => NotAcceptable("Token broken or expired")

    //409
    case UserAlreadyExist(msg) => Conflict(msg)

    //417
    case WrongCredentials => ExpectationFailed

    //422
    case WrongFormException(e) => UnprocessableEntity("Wrong json " + e)
    case e: IllegalArgumentException => UnprocessableEntity("Wrong id")


    //500
    case ex: Throwable =>
      println(ex)
      ex.printStackTrace()
      InternalServerError
  }

}