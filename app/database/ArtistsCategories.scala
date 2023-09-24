package database

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.PostgresProfile
import slick.lifted.ProvenShape

import java.util.UUID

trait ArtistsCategories extends HasDatabaseConfigProvider[PostgresProfile] {
  val ArtistsCategoriesTable = ArtistsCategories.ArtistsCategoriesTable

}

object ArtistsCategories {
  case class ArtistsCategoriesRow(
      id: UUID,
      artist_id: UUID,
      category_id: UUID
  ) {}

  import PostgresProfile.api._

  val ArtistsCategoriesTable = TableQuery[ArtistsCategoriesTableDef]

  class ArtistsCategoriesTableDef(tag: Tag) extends Table[ArtistsCategoriesRow](tag, "artists_categories") {
    def id: Rep[UUID] = column[UUID]("artist_category_id", O.PrimaryKey)
    def artist_id: Rep[UUID] = column[UUID]("artist_id_fk")
    def category_id: Rep[UUID] = column[UUID]("category_id_fk")

    override def * : ProvenShape[ArtistsCategoriesRow] = (id, artist_id, category_id).<>(ArtistsCategoriesRow.tupled, ArtistsCategoriesRow.unapply)

  }
}
