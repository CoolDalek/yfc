package daos

import java.util.UUID

import models.Post
import monix.eval.Task

trait PostDAO {

  def create(post: Post): Task[Option[Int]]

  def getAll(authorId: Long): Task[Seq[Post]]

  def getById(id: UUID): Task[Option[Post]]

  def update(post: Post): Task[Option[Post]]

  def delete(id: UUID): Task[Option[Post]]

}
