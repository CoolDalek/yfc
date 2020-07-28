package daos.mongo

import java.util.UUID

import daos.PostDAO
import javax.inject.{Inject, Singleton}
import models.Post
import monix.eval.Task
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.commands.WriteResult

import scala.concurrent.ExecutionContext

@Singleton
class PostDAOImpl @Inject()(reactiveMongoApi: ReactiveMongoApi)
                           (implicit ex: ExecutionContext)
  extends PostDAO {

  import daos.helper.Helpers._
  import reactivemongo.play.json.compat
  import compat.json2bson._

  private def posts: Task[BSONCollection] = reactiveMongoApi.database.map(_.collection("post")).wrapEx

  override def create(post: Post): Task[WriteResult] = posts.flatMap {
    _.insert.one(post).wrapEx
  }

  override def getAll(authorId: Long): Task[Seq[Post]] = posts.flatMap {
    _.find(BSONDocument("authorId" -> authorId)).cursor[Post]().collect[Seq]().wrapEx
  }

  override def getById(id: UUID): Task[Option[Post]] = posts.flatMap {
    _.find(BSONDocument("id" -> id)).one[Post].wrapEx
  }

  override def update(post: Post): Task[Option[Post]] = {
    val updateModifier = BSONDocument(
      f"$$set" -> BSONDocument(
        "title" -> post.title,
        "body" -> post.body,
        "lastUpdated" -> post.lastUpdated,
      )
    )
    posts.flatMap {
      _.findAndUpdate(
        selector = BSONDocument("id" -> post.id),
        update = updateModifier,
        fetchNewObject = true
      ).map(_.result[Post]).wrapEx
    }
  }

  override def delete(id: UUID): Task[Option[Post]] = posts.flatMap {
    _.findAndRemove(
      selector = BSONDocument("id" -> id)
    ).map(_.result[Post]).wrapEx
  }

}
