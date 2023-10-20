package models

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
  import play.api.libs.json._

  case class Create(
      description: String,
      artist_id: String,
      client_id: String,
      category_id: String
  )
  implicit val appointmentWrites: OWrites[Appointment] = Json.writes[Appointment]
  implicit val createAppointmentReads: Reads[Appointment.Create] = Json.reads[Create]

  case class Update(
      description: Option[String] = None
  )

    implicit val updateAppointmentReads: Reads[Update] = (
        DESCRIPTION
    )(Appointment.Update.apply _).withAtLeastOneAttribute

}
