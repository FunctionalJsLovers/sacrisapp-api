package models

import java.util.UUID

case class Product(
    id: UUID,
    name: String,
    price: Int,
    artist_id: UUID,
) {}

object Product {
  import play.api.libs.json._

  implicit val productWrites: OWrites[Product] = Json.writes[Product]
}
