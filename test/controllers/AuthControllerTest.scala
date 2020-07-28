package controllers

import controllers.filters.CustomRequest
import exceptions.Exceptions.{UserAlreadyExist, WrongCredentials}
import helpers.{BCryptHelper, TimeHelper}
import models.dto.{SignInDTO, UserDTO}
import monix.eval.Task
import monix.execution.Scheduler
import org.scalamock.scalatest.MockFactory
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.AnyContent
import play.api.mvc.Results._
import play.api.test.FakeRequest
import play.api.test.Helpers.stubControllerComponents
import services.AuthService
import testUtils.AsyncUtils

import scala.concurrent.ExecutionContext

class AuthControllerTest extends PlaySpec with MockFactory with AsyncUtils {

  implicit val ex: ExecutionContext = ExecutionContext.global
  implicit val sch: Scheduler = Scheduler.Implicits.global

  val authServiceMock: AuthService = mock[AuthService]

  val controller: AuthController = new AuthController(stubControllerComponents(), authServiceMock)

  "singIn" should {

    def fakeSignInRequest(email: String, password: String): FakeRequest[AnyContent] = {
      val jsBody = JsObject(
        Map(
          "email" -> JsString(email),
          "password" -> JsString(password)
        )
      )
      FakeRequest().withBody(AnyContent(jsBody))
    }

    "return Ok with userId in session" in {
      val correctRequest = fakeSignInRequest("some@mail.com", "12345678")
      val correctDTO = SignInDTO.form.bindFromRequest()(correctRequest).value.get

      authServiceMock.signIn _ expects correctDTO returns Task.now(1)

      controller.signIn.apply(correctRequest).get mustBe Ok.withSession(CustomRequest.CookieUserId -> "1")
    }

    "return ExpectationFailed" in {
      val incorrectCredentials = fakeSignInRequest("some@mail.com", "badpassword")
      val incorrectDTO = SignInDTO.form.bindFromRequest()(incorrectCredentials).value.get

      authServiceMock.signIn _ expects incorrectDTO returns Task.raiseError(WrongCredentials)

      controller.signIn.apply(incorrectCredentials).get mustBe ExpectationFailed
    }

    "return UnprocessableEntity(Wrong json + exception)" in {
      val incorrectRequest = fakeSignInRequest("some@mail.com", "badpass")
      val formErrors = SignInDTO.form.bindFromRequest()(incorrectRequest).errors

      controller.signIn.apply(incorrectRequest).get mustBe UnprocessableEntity("Wrong json " + formErrors)
    }

  }

  "singOut" should {

    "return Ok with new session" in {
      controller.signOut.apply(FakeRequest()).get mustBe Ok.withNewSession
    }

  }

  "singUp" should {

    def fakeSignUpRequest(nickname: String, email: String, password: String): FakeRequest[AnyContent] = {
      val jsBody = JsObject(
        Map(
          "nickname" -> JsString(nickname),
          "email" -> JsString(email),
          "password" -> JsString(password)
        )
      )
      FakeRequest().withBody(AnyContent(jsBody))
    }

    "return Ok" in {
      val correctRequest = fakeSignUpRequest("nick", "some@mail.com", "12345678")
      val correctDTO = UserDTO.form.bindFromRequest()(correctRequest).value.get

      (authServiceMock.signUp(_: UserDTO)(_: TimeHelper, _:BCryptHelper))
        .expects(correctDTO, TimeHelper.defaultTimeHelper, BCryptHelper.defaultBCryptHelper)
        .returns(Task.now(()))

      controller.signUp.apply(correctRequest).get mustBe Ok
    }

    "return Conflict(User already exist)" in {
      val correctRequest = fakeSignUpRequest("nick", "some@mail.com", "12345678")
      val correctDTO = UserDTO.form.bindFromRequest()(correctRequest).value.get

      (authServiceMock.signUp(_: UserDTO)(_: TimeHelper, _:BCryptHelper))
        .expects(correctDTO, TimeHelper.defaultTimeHelper, BCryptHelper.defaultBCryptHelper)
        .returns(Task.raiseError(UserAlreadyExist()))

      controller.signUp.apply(correctRequest).get mustBe Conflict("User already exist")
    }

    "return UnprocessableEntity(Wrong json + exception)" in {
      val incorrectRequest = fakeSignUpRequest("nick", "some@mail.com", "badpass")
      val formErrors = UserDTO.form.bindFromRequest()(incorrectRequest).errors

      controller.signUp.apply(incorrectRequest).get mustBe UnprocessableEntity("Wrong json " + formErrors)
    }

  }

  "confirm" should {

    "return Ok" in {
      val token = "token"

      (authServiceMock.confirm(_: String)(_: TimeHelper)) expects(token, TimeHelper.defaultTimeHelper) returns Task.now(())

      controller.confirm(token).apply(FakeRequest()).get mustBe Ok
    }

  }

}
