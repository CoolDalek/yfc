package models

import java.time.Instant

import helpers.TimeHelper
import models.dto.UserDTO

case class User(
                 id: Long,
                 nickname: String,
                 email: String,
                 password: String,
                 isActive: Boolean,
                 createdAt: Instant,
                 lastUpdated: Instant
               ) {

  def update(dto: UserDTO)(implicit th: TimeHelper): User = this.copy(
    nickname = dto.nickname,
    email = dto.email,
    password = dto.password,
    lastUpdated = th.now()
  )

  def activateUser: User = this.copy(isActive = true)

}

object User {

  def tupled: ((Long, String, String, String, Boolean, Instant, Instant)) => User = (this.apply _).tupled

}
