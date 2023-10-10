package models

import java.util.UUID

case class Admin(
    id: UUID,
    name: String,
    phone: String,
    email: String,
)

object Admin {
  import play.api.libs.json._
  implicit val adminWrites: OWrites[Admin] = Json.writes[Admin]

  case class Create(
    name: String,
    phone: String,
    email: String,
  )

  implicit val createAdminReads: Reads[Create] = Json.reads[Create]

}
