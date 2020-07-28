package models

import java.time.Instant
import java.util.UUID

import helpers.TimeHelper
import models.dto.PostDTO
import play.api.libs.json.{JsValue, Json, OFormat}

case class Post(
                 id: UUID,
                 authorId: Long,
                 title: String,
                 body: String,
                 createdAt: Instant,
                 lastUpdated: Instant
               ) {

  def update(dto: PostDTO, authorId: Long)(implicit th: TimeHelper): Post = this.copy(
    authorId = authorId,
    title = dto.title,
    body = dto.body,
    lastUpdated = th.now()
  )

  def toJson: JsValue = Json.toJson(this)

}

object Post {

  implicit val format: OFormat[Post] = Json.format[Post]

}
