package models.dto

import helpers.TimeHelper
import models.User

case class UserDTO(
                    nickname: String,
                    email: String,
                    password: String
                  ) {

  def toUser()(implicit th: TimeHelper) = {
    val now = th.now()
    User(
      id = -1,
      nickname = this.nickname,
      email = this.email,
      password = this.password,
      isActive = false,
      createdAt = now,
      lastUpdated = now
    )
  }

}

object UserDTO {

  import play.api.data.Form
  import play.api.data.Forms._

  implicit val form: Form[UserDTO] = Form(
    mapping(
      "nickname" -> nonEmptyText(1, 64),
      "email" -> email,
      "password" -> nonEmptyText(8, 64)
    )(UserDTO.apply)(UserDTO.unapply)
  )

}