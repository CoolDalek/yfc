package daos

import monix.eval.Task

trait CRUDRepository[T] {

  def create(model: T): Task[T]

  def getAll: Task[Seq[T]]

  def getById(id: Long): Task[Option[T]]

  def update(model: T): Task[Int]

  def delete(id: Long): Task[Int]

}
