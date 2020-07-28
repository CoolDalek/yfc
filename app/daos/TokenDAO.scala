package daos

import models.Token
import monix.eval.Task

trait TokenDAO extends CRUDRepository[Token] {

  def getByBody(body: String): Task[Option[Token]]

}
