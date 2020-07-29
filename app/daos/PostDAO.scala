package daos

import java.util.UUID

import models.Post
import monix.eval.Task

trait PostDAO {

  def create(post: Post): Task[Post]

  def getAll(authorId: Long): Task[Seq[Post]]

  def getById(id: UUID): Task[Option[Post]]

  def update(post: Post): Task[Post]

  def delete(id: UUID): Task[Post]

}
