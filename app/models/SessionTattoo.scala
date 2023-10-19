package models

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{OFormat, Reads}
import utils.ModelJson._

import java.time.LocalDateTime
import java.util.UUID

case class SessionTattoo(
    id: UUID,
    date: LocalDateTime,
    estimated_time: Int,
    status: String,
    price: Int,
    appointment_id: UUID
) {}

object SessionTattoo {
  import play.api.libs.json.Json

  case class Create(
      date: LocalDateTime,
      estimated_time: Int,
      status: String,
      price: Int,
      appointment_id: String
  )
  implicit val createSessionReads: OFormat[SessionTattoo.Create] = Json.format[Create]
  implicit val sessionFormat: OFormat[SessionTattoo] = Json.format[SessionTattoo]

  case class Update(
      date: Option[LocalDateTime] = None,
      estimated_time: Option[Int] = None,
      status: Option[String] = None,
      price: Option[Int] = None
  )

  implicit val updateSessionReads: Reads[Update] = (
    DATE and
      ESTIMATED_TIME and
      STATUS and
      PRICE
  )(SessionTattoo.Update.apply _).withAtLeastOneAttribute

}
