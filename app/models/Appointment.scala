package models

import play.api.libs.json.{Json, OFormat, Reads}
import utils.ModelJson._

import java.util.UUID

case class Appointment(
    id: UUID,
    description: String,
    artist_id: UUID,
    client_id: UUID,
    category_id: UUID
) {}

object Appointment {

  case class Create(
      description: String,
      artist_id: String,
      client_id: String,
      category_id: String
  )
  implicit val createAppointmentReads: OFormat[Appointment.Create] = Json.format[Create]
  implicit val appointmentFormat: OFormat[Appointment] = Json.format[Appointment]
  case class Update(
      description: Option[String] = None
  )

  implicit val updateAppointmentReads: Reads[Appointment.Update] =
    DESCRIPTION.map(description => Update(description))

}
