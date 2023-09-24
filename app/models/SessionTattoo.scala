package models

import play.api.libs.json.OFormat

import java.time.LocalDate
import java.util.UUID

case class SessionTattoo(
    id: UUID,
    date: LocalDate,
    estimated_time: Int,
    status: String,
    price: Int,
    appointmentId: UUID
) {}

object SessionTattoo {
  import play.api.libs.json.Json

  implicit val sessionFormat: OFormat[SessionTattoo] = Json.format[SessionTattoo]
}
