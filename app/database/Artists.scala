package database

import models.Artist
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.PostgresProfile
import slick.lifted.ProvenShape

import java.util.UUID

trait Artists extends HasDatabaseConfigProvider[PostgresProfile] {
  val ArtistsTable = Artists.ArtistsTable
}

object Artists {
  import PostgresProfile.api._
  val ArtistsTable = TableQuery[ArtistsTableDef]

  class ArtistsTableDef(tag: Tag) extends Table[Artist](tag, "artists") {
    def id: Rep[UUID] = column[UUID]("artist_id", O.PrimaryKey)
    def name: Rep[String] = column[String]("name")
    def phone: Rep[String] = column[String]("phone")
    def email: Rep[String] = column[String]("email")

    def adminId: Rep[UUID] = column[UUID]("admin_id_fk")

    override def * : ProvenShape[Artist] = (id, name, phone, email, adminId)
      .<>((Artist.apply _).tupled, Artist.unapply)
  }
}
