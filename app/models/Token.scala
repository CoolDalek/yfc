package models

import java.time.Instant

import helpers.{TimeHelper, TokenHelper}

case class Token(
                  userId: Long,
                  body: String,
                  createdAt: Instant
                ) {

  def isExpired(implicit tokenTTL: Long, timeH: TimeHelper): Boolean = createdAt.isBefore(timeH.now().minusMillis(tokenTTL))

}

object Token {

  def generate(userId: Long)(implicit tokenH: TokenHelper, timeH: TimeHelper): Token =
    Token(
      userId,
      tokenH.generateToken(),
      timeH.now()
    )

  def tupled: ((Long, String, Instant)) => Token = (this.apply _).tupled

}