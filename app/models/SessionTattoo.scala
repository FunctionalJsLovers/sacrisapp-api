package models

import play.api.libs.json.OFormat

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

case class SessionTattoo(
    id: UUID,
    date: LocalDateTime,
    estimated_time: Int,
    status: String,
    price: Int,
    appointmentId: UUID
) {}

object SessionTattoo {
  import play.api.libs.json.Json

  case class Create(
      date: LocalDateTime,
      estimated_time: Int,
      status: String,
      price: Int,
      appointmentId: String
  )
  implicit val createSessionReads: OFormat[SessionTattoo.Create] = Json.format[Create]
  implicit val sessionFormat: OFormat[SessionTattoo] = Json.format[SessionTattoo]
}
