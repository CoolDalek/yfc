package services

import java.util.UUID

import helpers.TimeHelper
import models.Post
import models.dto.PostDTO
import monix.eval.Task

trait PostService {

  def create(currentUserId: Long, postDTO: PostDTO)(implicit th: TimeHelper): Task[Post]

  def getAll(currentUserId: Long): Task[Seq[Post]]

  def getById(postId: UUID, currentUserId: Long): Task[Post]

  def update(postId: UUID, currentUserId: Long, postDTO: PostDTO)(implicit th: TimeHelper): Task[Unit]

  def delete(postId: UUID, currentUserId: Long): Task[Unit]

}
