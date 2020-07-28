package daos.slick

import daos.TokenDAO
import javax.inject.{Inject, Singleton}
import models.Token
import monix.eval.Task
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

@Singleton
class TokenDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                            (implicit ex: ExecutionContext)
  extends Tables(dbConfigProvider) with TokenDAO with HasDatabaseConfigProvider[JdbcProfile] {

  import daos.helper.Helpers._
  import profile.api._

  private val tokensQuery = tokens returning tokens

  override def create(token: Token): Task[Token] = db.run(tokensQuery += token).wrapEx

  override def getAll: Task[Seq[Token]] = db.run(tokens.result).wrapEx

  override def getById(userId: Long): Task[Option[Token]] = db.run(tokens.filter(_.userId === userId).result.headOption).wrapEx

  override def getByBody(body: String): Task[Option[Token]] = db.run(tokens.filter(_.body === body).result.headOption).wrapEx

  override def update(token: Token): Task[Int] = db.run(tokens.filter(_.userId === token.userId).update(token).checkRowsAffected).wrapEx

  override def delete(userId: Long): Task[Int] = db.run(tokens.filter(_.userId === userId).delete.checkRowsAffected).wrapEx

}
