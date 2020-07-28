package helpers

import com.github.t3hnar.bcrypt._

trait BCryptHelper {

  def encrypt(password: String, rounds: Int): String

}

object BCryptHelper {

  implicit val defaultBCryptHelper: BCryptHelper = (password: String, rounds: Int) => password.bcrypt(rounds)

}