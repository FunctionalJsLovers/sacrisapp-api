package models

import models.Admin.Create

import java.util.UUID

case class Client(
    id: UUID,
    name: String,
    phone: String,
    email: String,
) {}

object Client {
  import play.api.libs.json._

  case class Create(
      name: String,
      phone: String,
      email: String,
  )

  implicit val artistWrites: OWrites[Client] = Json.writes[Client]
  implicit val createClientReads: Reads[Create] = Json.reads[Create]

}
