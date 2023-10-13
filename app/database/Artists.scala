package database

import models.Artist
import play.api.db.slick.HasDatabaseConfigProvider
import slick.lifted.ProvenShape
import utils.PostgresProfile

import java.util.UUID

trait Artists extends HasDatabaseConfigProvider[PostgresProfile] {
  val ArtistsTable = Artists.ArtistsTable
}

object Artists {
  import utils.PostgresProfile.api._
  val ArtistsTable = TableQuery[ArtistsTableDef]

  class ArtistsTableDef(tag: Tag) extends Table[ArtistRow](tag, "artists") {
    def id: Rep[UUID] = column[UUID]("artist_id", O.PrimaryKey)
    def name: Rep[String] = column[String]("name")
    def phone: Rep[String] = column[String]("phone")
    def email: Rep[String] = column[String]("email")
    def adminId: Rep[UUID] = column[UUID]("admin_id_fk")
    def description: Rep[String] = column[String]("description")
    def instagram: Rep[String] = column[String]("instagram")
    def username: Rep[String] = column[String]("username")

    override def * : ProvenShape[ArtistRow] = (id, name, phone, email, adminId, description, instagram, username).mapTo[ArtistRow]

  }
  case class ArtistRow(
      id: UUID,
      name: String,
      phone: String,
      email: String,
      adminId: UUID,
      description: String,
      instagram: String,
      username: String,
  ) extends Product
      with Serializable {

    def toArtist: Artist = Artist(
      id,
      name,
      phone,
      email,
      adminId,
      description,
      instagram,
      username
    )
  }
}
