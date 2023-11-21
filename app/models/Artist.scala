package models

import java.util.UUID
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import utils.ModelJson._

case class Artist(
    id: UUID,
    name: String,
    phone: String,
    email: String,
    admin_id: UUID,
    description: String,
    instagram: String,
    username: String,
    categories: Seq[String] = Seq.empty,
) {}

object Artist {
  import play.api.libs.json._

  implicit val artistWrites: OWrites[Artist] = Json.writes[Artist]

  case class Create(
      name: String,
      phone: String,
      email: String,
      admin_id: UUID,
      description: String,
      instagram: String,
      username: String
  ) {}
  implicit val createArtistReads: Reads[Create] = Json.reads[Create]

  case class Update(
      name: Option[String] = None,
      phone: Option[String] = None,
      email: Option[String] = None,
      description: Option[String] = None,
      instagram: Option[String] = None,
      username: Option[String] = None
  )

  implicit val updateArtistReads: Reads[Update] = (
    NAME and
      PHONE and
      EMAIL and
      DESCRIPTION and
      INSTAGRAM and
      USERNAME
  )(Artist.Update.apply _).withAtLeastOneAttribute

  case class ArtistMap(artist: Map[String, Int])
  implicit val artistResponseWrites: OWrites[ArtistMap] = Json.writes[ArtistMap]


}
