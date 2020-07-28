package daos.slick

import daos.UserDAO
import javax.inject.{Inject, Singleton}
import models.User
import monix.eval.Task
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

@Singleton
class UserDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                           (implicit ex: ExecutionContext)
  extends Tables(dbConfigProvider) with UserDAO with HasDatabaseConfigProvider[JdbcProfile]  {

  import daos.helper.Helpers._
  import profile.api._

  private val usersQuery = users returning users

  override def getById(userId: Long): Task[Option[User]] =
    db.run(users.filter(_.id === userId).result.headOption).wrapEx

  override def getAll: Task[Seq[User]] = db.run(users.result).wrapEx

  override def getByEmail(email: String): Task[Option[User]] = db.run(users.filter(_.email === email).result.headOption).wrapEx

  override def create(user: User): Task[User] = db.run(usersQuery += user).wrapEx

  override def update(user: User): Task[Int] = db.run(users.filter(_.id === user.id).update(user).checkRowsAffected).wrapEx

  override def delete(id: Long): Task[Int] = db.run(users.filter(_.id === id).delete.checkRowsAffected).wrapEx

}
