package daos.mongo

import java.util.UUID

import daos.PostDAO
import exceptions.Exceptions.DbResultException
import javax.inject.{Inject, Singleton}
import models.Post
import monix.eval.Task
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.collection.BSONCollection

import scala.concurrent.ExecutionContext

@Singleton
class PostDAOImpl @Inject()(reactiveMongoApi: ReactiveMongoApi)
                           (implicit ex: ExecutionContext)
  extends PostDAO {

  import daos.helper.Helpers._
  import reactivemongo.play.json.compat
  import compat.json2bson._

  private def posts: Task[BSONCollection] = reactiveMongoApi.database.map(_.collection("post")).wrapEx

  override def create(post: Post): Task[Post] = posts.flatMap {
    _.insert.one(post).map(_.code).wrapEx.flatMap {
      case None => Task.now(post)
      case Some(errCode) => Task.raiseError(DbResultException)
    }
  }

  override def getAll(authorId: Long): Task[Seq[Post]] = posts.flatMap {
    _.find(BSONDocument("authorId" -> authorId)).cursor[Post]().collect[Seq]().wrapEx
  }

  override def getById(id: UUID): Task[Option[Post]] = posts.flatMap {
    _.find(BSONDocument("_id" -> id)).one[Post].wrapEx
  }

  override def update(post: Post): Task[Post] = {
    val updateModifier = BSONDocument(
      f"$$set" -> BSONDocument(
        "title" -> post.title,
        "body" -> post.body,
        "lastUpdated" -> post.lastUpdated,
      )
    )
    posts.flatMap {
      _.findAndUpdate(
        selector = BSONDocument("_id" -> post._id),
        update = updateModifier,
        fetchNewObject = true
      ).map(_.result[Post]).wrapEx.flatMap {
        case None => Task.raiseError(DbResultException)
        case Some(post) => Task.now(post)
      }
    }
  }

  override def delete(id: UUID): Task[Post] = posts.flatMap {
    _.findAndRemove(
      selector = BSONDocument("_id" -> id)
    ).map(_.result[Post]).wrapEx.flatMap {
      case None => Task.raiseError(DbResultException)
      case Some(post) => Task.now(post)
    }
  }

}
