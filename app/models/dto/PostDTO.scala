package models.dto

import java.util.UUID

import helpers.TimeHelper
import models.Post

case class PostDTO(
                    authorId: Long,
                    title: String,
                    body: String
                  ) {

  def toPost()(implicit th: TimeHelper) = {
    val now = th.now()
    Post(
      id = UUID.randomUUID(),
      authorId = this.authorId,
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
      "authorId" -> longNumber,
      "title" -> text(0, 50),
      "body" -> nonEmptyText(1, 500)
    )(PostDTO.apply)(PostDTO.unapply)
  )

}
