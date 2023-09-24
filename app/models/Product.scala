package models

import java.util.UUID

case class Product(
    id: UUID,
    name: String,
    price: Int,
    artistId: UUID,
) {}

object Product {
  import play.api.libs.json._

  implicit val productWrites: OWrites[Product] = Json.writes[Product]
}
