package models

import java.util.UUID

case class Appointment(
    id: UUID,
    description: String,
    artistId: UUID,
    clientId: UUID,
    categoryId: UUID
) {}

object Appointment {
  import play.api.libs.json._
  implicit val appointmentWrites: OWrites[Appointment] = Json.writes[Appointment]
}
