package controllers

import java.util.UUID

import controllers.filters.CustomRequest
import exceptions.Exceptions.{ForbiddenException, NotFoundException}
import helpers.TimeHelper
import models.Post
import models.dto.PostDTO
import monix.eval.Task
import monix.execution.Scheduler
import org.scalamock.scalatest.MockFactory
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsArray, JsObject, JsString}
import play.api.mvc.Results._
import play.api.mvc.{AnyContent, AnyContentAsEmpty}
import play.api.test.FakeRequest
import play.api.test.Helpers.stubControllerComponents
import services.PostService
import testUtils.AsyncUtils

import scala.concurrent.ExecutionContext

class PostControllerTest extends PlaySpec with MockFactory with AsyncUtils  {

  implicit val ex: ExecutionContext = ExecutionContext.global
  implicit val sch: Scheduler = Scheduler.Implicits.global

  val postServiceMock: PostService = mock[PostService]

  val controller: PostController = new PostController(stubControllerComponents(), postServiceMock)

  val userId: Long = 1
  def fakePostRequest(title: String, body: String): FakeRequest[AnyContent] = {
    val jsBody = JsObject(
      Map(
        "title" -> JsString(title),
        "body" -> JsString(body)
      )
    )
    FakeRequest().withBody(AnyContent(jsBody)).withSession(CustomRequest.CookieUserId -> userId.toString)
  }
  val correctDTO: PostDTO = PostDTO("title", "body")
  val correctRequest: FakeRequest[AnyContent] = fakePostRequest(correctDTO.title, correctDTO.body)
  val incorrectRequest: FakeRequest[AnyContent] = fakePostRequest("", "")
  val unauthorizedRequest: FakeRequest[AnyContent] = FakeRequest()
  val authorizedRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession(CustomRequest.CookieUserId -> userId.toString)
  val post: Post = correctDTO.toPost(userId)
  val postId: UUID = UUID.randomUUID()
  val wrongId: String = "wrongID"

  "create" should {

    "return Ok with post" in {
      (postServiceMock.create(_ : Long, _: PostDTO)(_ :TimeHelper))
        .expects(userId, correctDTO, TimeHelper.defaultTimeHelper)
        .returns(Task.now(post))

      controller.create.apply(correctRequest).get mustBe Ok(post.toJson)
    }

    "return Unauthorized" in {
      controller.create.apply(unauthorizedRequest).get mustBe Unauthorized
    }

    "return UnprocessableEntity(Wrong json + exception)" in {
      val formErrors = PostDTO.form.bindFromRequest()(incorrectRequest).errors

      controller.create.apply(incorrectRequest).get mustBe UnprocessableEntity("Wrong json " + formErrors)
    }

  }

  "getAll" should {

    "return Ok with Seq(post)" in {
      postServiceMock.getAll _ expects userId returns Task.now(Seq(post))

      controller.getAll.apply(authorizedRequest).get mustBe Ok(JsArray(Seq(post.toJson)))
    }

    "return Unauthorized" in {
      controller.getAll.apply(unauthorizedRequest).get mustBe Unauthorized
    }

  }

  "getById" should {

    "return Ok with post" in {
      (postServiceMock.getById(_: UUID, _: Long)) expects(postId, userId) returns Task.now(post)

      controller.getById(postId.toString).apply(authorizedRequest).get mustBe Ok(post.toJson)
    }

    "return Unauthorized" in {
      controller.getById(postId.toString).apply(unauthorizedRequest).get mustBe Unauthorized
    }

    "return Forbidden" in {
      (postServiceMock.getById(_: UUID, _: Long))
        .expects(postId, userId) returns Task.raiseError(ForbiddenException("Not your post"))

      controller.getById(postId.toString).apply(authorizedRequest).get mustBe Forbidden("Not your post")
    }

    "return NotFound" in {
      (postServiceMock.getById(_: UUID, _: Long))
        .expects(postId, userId) returns Task.raiseError(NotFoundException("Post", s"id = $postId"))

      controller.getById(postId.toString).apply(authorizedRequest).get mustBe NotFound(s"Post with id = $postId not found!")
    }

    "return UnprocessableEntity(Wrong id)" in {
      controller.getById(wrongId).apply(authorizedRequest).get mustBe UnprocessableEntity("Wrong id")
    }

  }

  "update" should {

    "return Ok" in {
      (postServiceMock.update(_: UUID, _: Long, _: PostDTO)(_: TimeHelper))
        .expects(postId, userId, correctDTO, TimeHelper.defaultTimeHelper)
        .returns(Task.now(post))

      controller.update(postId.toString).apply(correctRequest).get mustBe Ok(post.toJson)
    }

    "return Unauthorized" in {
      controller.update(postId.toString).apply(unauthorizedRequest).get mustBe Unauthorized
    }

    "return Forbidden" in {
      (postServiceMock.update(_: UUID, _: Long, _: PostDTO)(_: TimeHelper))
        .expects(postId, userId, correctDTO, TimeHelper.defaultTimeHelper)
        .returns(Task.raiseError(ForbiddenException("Not your post")))

      controller.update(postId.toString).apply(correctRequest).get mustBe Forbidden("Not your post")
    }

    "return NotFound" in {
      (postServiceMock.update(_: UUID, _: Long, _: PostDTO)(_: TimeHelper))
        .expects(postId, userId, correctDTO, TimeHelper.defaultTimeHelper)
        .returns(Task.raiseError(NotFoundException("Post", s"id = $postId")))

      controller.update(postId.toString).apply(correctRequest).get mustBe NotFound(s"Post with id = $postId not found!")
    }

    "return UnprocessableEntity(Wrong id)" in {
      controller.update(wrongId).apply(correctRequest).get mustBe UnprocessableEntity("Wrong id")
    }

  }

  "delete" should {

    "return Ok" in {
      (postServiceMock.delete(_: UUID, _: Long)) expects(postId, userId) returns Task.now(())

      controller.delete(postId.toString).apply(authorizedRequest).get mustBe Ok
    }

    "return Unauthorized" in {
      controller.delete(postId.toString).apply(unauthorizedRequest).get mustBe Unauthorized
    }

    "return Forbidden" in {
      (postServiceMock.delete(_: UUID, _: Long)).expects(postId, userId)
        .returns(Task.raiseError(ForbiddenException("Not your post")))

      controller.delete(postId.toString).apply(authorizedRequest).get mustBe Forbidden("Not your post")
    }

    "return NotFound" in {
      (postServiceMock.delete(_: UUID, _: Long)).expects(postId, userId)
        .returns(Task.raiseError(NotFoundException("Post", s"id = $postId")))

      controller.delete(postId.toString).apply(authorizedRequest).get mustBe NotFound(s"Post with id = $postId not found!")
    }

    "return UnprocessableEntity(Wrong id)" in {
      controller.delete(wrongId).apply(authorizedRequest).get mustBe UnprocessableEntity("Wrong id")
    }

  }

}
