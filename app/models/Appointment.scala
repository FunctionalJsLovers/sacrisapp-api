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

  case class Create(
      description: String,
      artist_id: String,
      client_id: String,
      category_id: String
  )
  implicit val appointmentWrites: OWrites[Appointment] = Json.writes[Appointment]
  implicit val createAppointmentReads: Reads[Appointment.Create] = Json.reads[Create]
}
