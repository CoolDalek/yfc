package helpers

import java.util.UUID

trait TokenHelper {

  def generateToken(): String

}

object TokenHelper {

  implicit val defaultTokenHelper: TokenHelper = () => UUID.randomUUID().toString

}