package models.dto

import java.util.UUID

import helpers.TimeHelper
import models.Post

case class PostDTO(
                    title: String,
                    body: String
                  ) {

  def toPost(authorId: Long)(implicit th: TimeHelper): Post = {
    val now = th.now()
    Post(
      _id = UUID.randomUUID(),
      authorId = authorId,
      title = this.title,
      body = this.body,
      createdAt = now,
      lastUpdated = now
    )
  }

}

object PostDTO {

  import play.api.data.Form
  import play.api.data.Forms._

  implicit val form: Form[PostDTO] = Form(
    mapping(
      "title" -> nonEmptyText(1, 50),
      "body" -> nonEmptyText(1, 500)
    )(PostDTO.apply)(PostDTO.unapply)
  )

}
