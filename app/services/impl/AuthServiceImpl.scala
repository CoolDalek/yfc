package services.impl

import com.github.t3hnar.bcrypt._
import daos.{TokenDAO, UserDAO}
import exceptions.Exceptions._
import helpers.{BCryptHelper, TimeHelper}
import javax.inject.{Inject, Singleton}
import models.dto.{SignInDTO, UserDTO}
import models.{Token, User}
import monix.eval.Task
import play.api.{Configuration, Logging}
import services.{AuthService, MailerService}

@Singleton
class AuthServiceImpl @Inject()(tokenDAO: TokenDAO, userDAO: UserDAO, mailerService: MailerService, config: Configuration) extends AuthService with Logging {

  def confirm(tokenBody: String)(implicit th: TimeHelper): Task[Unit] = {

    implicit val expirationTime: Long = config.get[Long]("tokenTTl")
    tokenDAO.getByBody(tokenBody).flatMap {

      case None => Task.raiseError(TokenBrokenOrExpired)

      case Some(token) if token.isExpired =>
        tokenDAO.delete(token.userId).flatMap { _ =>
          Task.raiseError(TokenBrokenOrExpired)
        }

      case Some(token) if !token.isExpired =>
        tokenDAO.delete(token.userId).flatMap { _ =>
          activateUser(token.userId)
        }

    }

  }

  private def activateUser(userId: Long)(implicit th: TimeHelper): Task[Unit] = {
    userDAO.getById(userId).flatMap {
      case None => Task.raiseError(InternalException("Non expired token is exist, but user is absent."))
      case Some(user) if user.isActive => Task.raiseError(InternalException("User already active, but token still exist."))
      case Some(user) if !user.isActive => userDAO.update(user.activateUser).map(_ => ())
    }
  }

  def signUp(dto: UserDTO)(implicit th: TimeHelper, BCh: BCryptHelper): Task[Unit] = {
    implicit val expirationTime: Long = config.get[Long]("tokenTTl")
    (for {
      userOption <- userDAO.getByEmail(dto.email)
      tokenOption <- userOption match {
        case Some(user) => tokenDAO.getById(user.id)
        case None => Task.now(None)
      }
    } yield {
      (userOption, tokenOption) match {
        case (Some(user), None) if user.isActive => Task.raiseError(UserAlreadyExist())
        case (Some(user), None) if !user.isActive =>
          logger.warn("The user is inactive and the token is expired or broken, sent new token.")
          createAndSendToken(user)
        case (Some(user), Some(token)) if token.isExpired =>
          tokenDAO.delete(token.userId).flatMap { _ =>
            createAndSendToken(user)
          }
        case (Some(user), Some(token)) if !token.isExpired =>
          mailerService.sendMail(user.email, token.body)
        case (None, _) =>
          userDAO.create(dto.toUser()).flatMap { user =>
            createAndSendToken(user)
          }
      }
    }).flatten
  }

  private def createAndSendToken(user: User): Task[Unit] =
    tokenDAO.create(Token.generate(user.id)).flatMap { token =>
      mailerService.sendMail(user.email, token.body)
    }


  def signIn(credentials: SignInDTO): Task[Long] = {
    userDAO.getByEmail(credentials.email) flatMap  {
      case Some(user) if !credentials.password.isBcrypted(user.password) => Task.raiseError(WrongCredentials)
      case Some(user) if !user.isActive => Task.raiseError(UserIsNotActive)
      case Some(user) => Task.now(user.id)
      case _ => Task.raiseError(WrongCredentials)
    }
  }

}
