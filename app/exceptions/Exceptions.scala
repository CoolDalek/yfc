package exceptions

import play.api.data.FormError

object Exceptions {

  case class DbException(e: Throwable) extends RuntimeException("DB exception", e)

  case object DbResultException extends RuntimeException("No raws was affected")

  case class UserAlreadyExist(message: String = "User already exist") extends RuntimeException(message)

  case object TokenBrokenOrExpired extends RuntimeException

  case object UserIsNotActive extends RuntimeException("User has't confirmed his email or was deactivated")

  case object WrongCredentials extends RuntimeException("Wrong credentials")

  case class InternalException(message: String = "Internal exception") extends RuntimeException(message)

  case class NotFoundException(message: String) extends RuntimeException(message)

  object NotFoundException {
    def apply(model: String, cond: String): NotFoundException = new NotFoundException(s"$model with $cond not found!")
  }

  case class ForbiddenException(message: String) extends RuntimeException("Forbidden. " + message)

  case object UnauthorizedException extends RuntimeException("Unauthorized")

  case class WrongFormException(errors: Seq[FormError]) extends RuntimeException(s"Wrong form.\n$errors")

}
