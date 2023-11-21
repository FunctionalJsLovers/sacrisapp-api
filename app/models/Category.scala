package models

import java.util.UUID

case class Category(
    id: UUID,
    name: String,
) {}

object Category {
  import play.api.libs.json._

  implicit val categoryWrites: OWrites[Category] = Json.writes[Category]

  case class Create(name: String) {}
  implicit val categoryCreateReads: Reads[Create] = Json.reads[Create]

  case class CategoryMap(category: Map[Int, UUID])
  implicit val categoryResponseWrites: OWrites[CategoryMap] = Json.writes[CategoryMap]
}
