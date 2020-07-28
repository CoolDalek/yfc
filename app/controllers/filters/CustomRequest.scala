package controllers.filters

import play.api.libs.Files
import play.api.mvc.{AnyContent, MultipartFormData, Request, WrappedRequest}

case class CustomRequest[T](userIdOption: Option[Long],
                            bodyOption: Option[T],
                            request: Request[AnyContent]) extends WrappedRequest (request){
  lazy val parsedBody: T = bodyOption.get
  lazy val userId: Long = userIdOption.get

  def withParsedBody[K](body: K): CustomRequest[K] = CustomRequest(
    userIdOption,
    Some(body),
    request
  )

}

object CustomRequest {
  val CookieUserId = "userId"
  type MultipartFile = MultipartFormData.FilePart[Files.TemporaryFile]
}