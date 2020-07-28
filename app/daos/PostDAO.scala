package daos

import java.util.UUID

import models.Post
import monix.eval.Task
import reactivemongo.api.commands.WriteResult

trait PostDAO {

  def create(post: Post): Task[WriteResult]

  def getAll(authorId: Long): Task[Seq[Post]]

  def getById(id: UUID): Task[Option[Post]]

  def update(post: Post): Task[Option[Post]]

  def delete(id: UUID): Task[Option[Post]]

}