package daos.slick

import java.time.Instant

import exceptions.Exceptions.DbResultException
import models.{Token, User}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.lifted
import slick.lifted.ProvenShape

import scala.concurrent.ExecutionContext

class Tables(dbConfigProvider: DatabaseConfigProvider) {

  val conf = dbConfigProvider.get[JdbcProfile]

  import conf.profile.api._

  implicit class SafeDB(action: conf.profile.ProfileAction[Int, NoStream, Effect.Write]) {

    def checkRowsAffected(implicit ex: ExecutionContext): DBIOAction[Int, NoStream, Effect.Write with Effect with Effect.Transactional] =
      action.flatMap { updatedRows =>
        if (updatedRows == 0) DBIO.failed(DbResultException)
        else DBIO.successful(updatedRows)
      }.transactionally

  }

  class UserTable(tag: Tag) extends Table[User](tag, "users") {
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def nickname: Rep[String] = column[String]("nickname")

    def email: Rep[String] = column[String]("email")

    def password: Rep[String] = column[String]("password")

    def isActive: Rep[Boolean] = column[Boolean]("is_active")

    def createdAt: Rep[Instant] = column[Instant]("created_at")

    def lastUpdated: Rep[Instant] = column[Instant]("last_updated")

    def * : ProvenShape[User] =
      (id, nickname, email, password, isActive, createdAt, lastUpdated) <> (User.tupled, User.unapply)
  }

  val users = lifted.TableQuery[UserTable]

  class TokenTable(tag: Tag) extends Table[Token](tag, "token") {

    def userId: Rep[Long] = column[Long]("user_id")

    def body: Rep[String] = column[String]("body")

    def createdAt: Rep[Instant] = column[Instant]("created_at")

    def * : ProvenShape[Token] = (userId, body, createdAt) <> (Token.tupled, Token.unapply)

  }

  val tokens = lifted.TableQuery[TokenTable]

}
