package models.dto

case class SignInDTO(
                      email: String,
                      password: String
                    )

object SignInDTO {

  import play.api.data.Form
  import play.api.data.Forms._

  implicit val form: Form[SignInDTO] = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText(8, 64)
    )(SignInDTO.apply)(SignInDTO.unapply)
  )

}