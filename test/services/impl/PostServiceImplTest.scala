package services.impl

import java.time.Instant

import daos.PostDAO
import exceptions.Exceptions._
import helpers.TimeHelper
import models.Post
import models.dto.PostDTO
import monix.eval.Task
import monix.execution.Scheduler
import org.scalamock.scalatest.MockFactory
import org.scalatestplus.play.PlaySpec
import testUtils.AsyncUtils

import scala.concurrent.ExecutionContext

class PostServiceImplTest extends PlaySpec with MockFactory with AsyncUtils {

  implicit val ex: ExecutionContext = ExecutionContext.global
  implicit val sch: Scheduler = Scheduler.Implicits.global
  implicit val th: TimeHelper = () => Instant.EPOCH

  val postDAOMock: PostDAO = mock[PostDAO]
  val service = new PostServiceImpl(postDAOMock)

  val userId: Long = 1
  val postDTO: PostDTO = PostDTO("title", "body")
  val post: Post = postDTO.toPost(userId)
  val postDTOMock: PostDTO = mock[PostDTO]

  "create" should {

    "return post" in {
      (postDTOMock.toPost(_:Long)(_:TimeHelper)) expects(userId, th) returns post
      postDAOMock.create _ expects post returns Task.now(post)

      service.create(userId, postDTOMock).get mustBe post
    }

  }

  "getAll" should {

    val posts = Seq(post)

    "return Seq(post)" in {
      postDAOMock.getAll _ expects userId returns Task.now(posts)

      service.getAll(userId).get mustBe posts
    }

  }

  "getById" should {

    "return post" in {
      postDAOMock.getById _ expects post._id returns Task.now(Some(post))

      service.getById(post._id, userId).get mustBe post
    }

    "raise NotFoundException" in {
      postDAOMock.getById _ expects post._id returns Task.now(None)

      a[NotFoundException] should be thrownBy service.getById(post._id, userId).get
    }

    "raise ForbiddenException" in {
      postDAOMock.getById _ expects post._id returns Task.now(Some(post))

      a[ForbiddenException] should be thrownBy service.getById(post._id, userId + 1).get
    }

  }

  "update" should {

    "return post" in {
      postDAOMock.getById _ expects post._id returns Task.now(Some(post))
      postDAOMock.update _ expects post returns Task.now(post)

      service.update(post._id, userId, postDTO).get mustBe post
    }

    "raise NotFoundException" in {
      postDAOMock.getById _ expects post._id returns Task.now(None)

      a[NotFoundException] should be thrownBy service.update(post._id, userId, postDTO).get
    }

    "raise ForbiddenException" in {
      postDAOMock.getById _ expects post._id returns Task.now(Some(post))

      a[ForbiddenException] should be thrownBy service.update(post._id, userId + 1, postDTO).get
    }

  }

  "delete" should {

    "return Unit" in {
      postDAOMock.getById _ expects post._id returns Task.now(Some(post))
      postDAOMock.delete _ expects post._id returns Task.now(post)

      service.delete(post._id, userId).get mustBe(())
    }

    "raise NotFoundException" in {
      postDAOMock.getById _ expects post._id returns Task.now(None)

      a[NotFoundException] should be thrownBy service.delete(post._id, userId).get
    }

    "raise ForbiddenException" in {
      postDAOMock.getById _ expects post._id returns Task.now(Some(post))

      a[ForbiddenException] should be thrownBy service.delete(post._id, userId + 1).get
    }

  }

}
