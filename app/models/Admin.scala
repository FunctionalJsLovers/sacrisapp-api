package models

import java.util.UUID

case class Admin(
    id: UUID,
    name: String,
    phone: String,
    email: String,
) {}

object Admin {
  import play.api.libs.json._

  implicit val adminWrites: OWrites[Admin] = Json.writes[Admin]
}
