package models

import java.util.UUID

case class ArtistCategory(
    id: UUID,
    artist_id: UUID,
    category_id: UUID
) {}
object ArtistCategory {
  import play.api.libs.json._
  case class Create(
      artist_id: String,
      category_id: String
  )

  implicit val createArtistCategoryReads: Reads[ArtistCategory.Create] = Json.reads[ArtistCategory.Create]
  implicit val artistCategoryWrites: OWrites[ArtistCategory] = Json.writes[ArtistCategory]

}
