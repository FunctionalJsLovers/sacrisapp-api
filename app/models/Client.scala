package models

import models.Admin.Create

import java.util.UUID
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import utils.ModelJson._

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

  case class Update(
      name: Option[String] = None,
      phone: Option[String] = None,
      email: Option[String] = None,
  )

  implicit val artistWrites: OWrites[Client] = Json.writes[Client]
  implicit val createClientReads: Reads[Create] = Json.reads[Create]

  implicit val updateClientReads: Reads[Update] = (
    NAME and
      PHONE and
      EMAIL
  )(Client.Update.apply _).withAtLeastOneAttribute

}
