package models

import java.util.UUID

case class Client(
    id: UUID,
    name: String,
    phone: String,
    email: String,
) {}

object Client {

  import play.api.libs.json._

  implicit val artistWrites: OWrites[Client] = Json.writes[Client]
}
