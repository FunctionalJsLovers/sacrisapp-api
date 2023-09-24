package models

import java.util.UUID

case class Category(
    id: UUID,
    name: String,
) {}

object Category {
  import play.api.libs.json._

  implicit val categoryWrites: OWrites[Category] = Json.writes[Category]
}
