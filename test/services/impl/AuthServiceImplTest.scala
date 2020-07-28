package services.impl

import java.time.Instant

import com.github.t3hnar.bcrypt._
import daos.{TokenDAO, UserDAO}
import exceptions.Exceptions._
import helpers.{BCryptHelper, TimeHelper, TokenHelper}
import models.dto.{SignInDTO, UserDTO}
import models.{Token, User}
import monix.eval.Task
import monix.execution.Scheduler
import org.scalamock.scalatest.MockFactory
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import services.MailerService
import testUtils.AsyncUtils

import scala.concurrent.ExecutionContext

class AuthServiceImplTest extends PlaySpec with MockFactory with AsyncUtils {

  implicit val ex: ExecutionContext = ExecutionContext.global
  implicit val sch: Scheduler = Scheduler.Implicits.global
  implicit val timeH: TimeHelper = () => Instant.EPOCH
  implicit val BCh: BCryptHelper = (_: String, _: Int) => "password"
  implicit val tokenH: TokenHelper = () => "token"

  val tokenTTl = 1
  val conf: Configuration = Configuration.from(Map("tokenTTl" -> tokenTTl))
  val tokenDAOMock: TokenDAO = mock[TokenDAO]
  val userDAOMock: UserDAO = mock[UserDAO]
  val mailerServiceMock: MailerService = mock[MailerService]
  val service = new AuthServiceImpl(tokenDAOMock, userDAOMock, mailerServiceMock, conf)

  val userDTO: UserDTO = UserDTO(
    "nick",
    "mail",
    "password",
  )
  val inactiveUser: User = User(
    -1,
    nickname = userDTO.nickname,
    email = userDTO.email,
    password = BCh.encrypt(userDTO.password, 1),
    isActive = false,
    timeH.now(),
    timeH.now()
  )
  val activeUser: User = inactiveUser.activateUser
  val userId: Long = 1
  val tokenBody: String = tokenH.generateToken()
  val nonExpiredToken: Token = Token.generate(inactiveUser.id)(timeH = () => Instant.MAX.minusMillis(1), tokenH = tokenH)
  val expiredToken: Token = Token.generate(inactiveUser.id)(timeH = () => Instant.MIN.plusMillis(1), tokenH = tokenH)
  val token: Token = Token.generate(inactiveUser.id)

  "confirm" should {

    "delete token and activate user" in {
      tokenDAOMock.getByBody _ expects tokenBody returns Task.now(Some(nonExpiredToken))
      tokenDAOMock.delete _ expects nonExpiredToken.userId returns Task.now(1)
      userDAOMock.getById _ expects nonExpiredToken.userId returns Task.now(Some(inactiveUser))
      userDAOMock.update _ expects activeUser returns Task.now(1)

      service.confirm(tokenBody).get mustBe(())
    }

    "raise TokenBrokenOrExpired because token not found" in {
      tokenDAOMock.getByBody _ expects tokenBody returns Task.now(None)

      a[TokenBrokenOrExpired.type] should be thrownBy service.confirm(tokenBody).get
    }

    "delete token and raise TokenBrokenOrExpired because token is expired" in {
      tokenDAOMock.getByBody _ expects tokenBody returns Task.now(Some(expiredToken))
      tokenDAOMock.delete _ expects nonExpiredToken.userId returns Task.now(1)

      a[TokenBrokenOrExpired.type] should be thrownBy service.confirm(tokenBody).get
    }

  }

  "signUp" should {

    "raise UserAlreadyExist exception" in {
      userDAOMock.getByEmail _ expects userDTO.email returns Task.now(Some(activeUser))
      tokenDAOMock.getById _ expects activeUser.id returns Task.now(None)

      a[UserAlreadyExist] should be thrownBy service.signUp(userDTO).get
    }

    "create and send new token (note: this case should never happen in prod)" in {
      userDAOMock.getByEmail _ expects userDTO.email returns Task.now(Some(inactiveUser))
      tokenDAOMock.getById _ expects inactiveUser.id returns Task.now(None)
      tokenDAOMock.create _ expects token returns Task.now(token)
      (mailerServiceMock.sendMail(_: String, _: String)) expects(inactiveUser.email, tokenBody) returns Task.now(())

      service.signUp(userDTO).get mustBe(())
    }

    "delete expired token, create and send new token" in {
      userDAOMock.getByEmail _ expects userDTO.email returns Task.now(Some(inactiveUser))
      tokenDAOMock.getById _ expects inactiveUser.id returns Task.now(Some(expiredToken))
      tokenDAOMock.delete _ expects expiredToken.userId returns Task.now(1)
      tokenDAOMock.create _ expects token returns Task.now(token)
      (mailerServiceMock.sendMail(_: String, _: String)) expects(inactiveUser.email, tokenBody) returns Task.now(())

      service.signUp(userDTO).get mustBe(())
    }

    "resend existing token" in {
      userDAOMock.getByEmail _ expects userDTO.email returns Task.now(Some(inactiveUser))
      tokenDAOMock.getById _ expects inactiveUser.id returns Task.now(Some(nonExpiredToken))
      (mailerServiceMock.sendMail(_: String, _: String)) expects(inactiveUser.email, tokenBody) returns Task.now(())

      service.signUp(userDTO).get mustBe(())
    }

    "create user, create and send token" in {
      userDAOMock.getByEmail _ expects userDTO.email returns Task.now(None)
      userDAOMock.create _ expects inactiveUser returns Task.now(inactiveUser)
      tokenDAOMock.create _ expects token returns Task.now(token)
      (mailerServiceMock.sendMail(_: String, _: String)) expects(inactiveUser.email, tokenBody) returns Task.now(())

      service.signUp(userDTO).get mustBe(())
    }

  }

  "singIn" should {

    val password: String = "password"
    val encryptedPassword = password.bcrypt
    val signInDTO: SignInDTO = SignInDTO(inactiveUser.email, password)
    val activeUserWithPass = activeUser.copy(password = encryptedPassword)
    val inactiveUserWithPass = inactiveUser.copy(password = encryptedPassword)
    val userWithInvalidPass =inactiveUser.copy(password = "$2a$10$passwordpasswordpasswordpasswordpasswordpasswordpassw")

    "return Task(userId)" in {
      userDAOMock.getByEmail _ expects signInDTO.email returns Task.now(Some(activeUserWithPass))

      service.signIn(signInDTO).get mustBe activeUserWithPass.id
    }

    "raise WrongCredentials because passwords not equals" in {
      userDAOMock.getByEmail _ expects signInDTO.email returns Task.now(Some(userWithInvalidPass))

      a[WrongCredentials.type] should be thrownBy service.signIn(signInDTO).get
    }

    "raise WrongCredentials because user is not found" in {
      userDAOMock.getByEmail _ expects signInDTO.email returns Task.now(None)

      a[WrongCredentials.type] should be thrownBy service.signIn(signInDTO).get
    }

    "raise UserIsNotActive" in {
      userDAOMock.getByEmail _ expects signInDTO.email returns Task.now(Some(inactiveUserWithPass))

      a[UserIsNotActive.type] should be thrownBy service.signIn(signInDTO).get}

  }

}
