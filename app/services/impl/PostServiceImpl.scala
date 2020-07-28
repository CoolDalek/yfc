package services.impl

import java.util.UUID

import daos.PostDAO
import exceptions.Exceptions.{ForbiddenException, NotFoundException}
import helpers.TimeHelper
import javax.inject.{Inject, Singleton}
import models.Post
import models.dto.PostDTO
import monix.eval.Task
import services.PostService

@Singleton
class PostServiceImpl @Inject()(postDAO: PostDAO) extends PostService {

  override def create(currentUserId: Long, postDTO: PostDTO)(implicit th: TimeHelper): Task[Post] = {
    val post = postDTO.toPost(currentUserId)
    postDAO.create(post).map(_ => post)
  }

  override def getAll(currentUserId: Long): Task[Seq[Post]] = postDAO.getAll(currentUserId)

  override def getById(postId: UUID, currentUserId: Long): Task[Post] = ifExistsAndOwner(postId, currentUserId)(Task.now)

  override def update(postId: UUID, currentUserId: Long, postDTO: PostDTO)(implicit th: TimeHelper): Task[Unit] =
    ifExistsAndOwner(postId, currentUserId) { post =>
      postDAO.update(post.update(postDTO, currentUserId)).map(_ => ())
    }

  override def delete(postId: UUID, currentUserId: Long): Task[Unit] = ifExistsAndOwner(postId, currentUserId) { _ =>
    postDAO.delete(postId).map(_ => ())
  }

  private def ifExistsAndOwner[T](postId: UUID, currentUserId: Long)(block: Post => Task[T]): Task[T] =
    postDAO.getById(postId).flatMap {
      case None => Task.raiseError(NotFoundException("Post", s"id = $postId"))
      case Some(p) if p.authorId != currentUserId => Task.raiseError(ForbiddenException("Not your post"))
      case Some(p) => block(p)
    }

}
