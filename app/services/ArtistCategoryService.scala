package services

import com.google.inject.Inject
import database.ArtistsCategories.{ArtistsCategoriesTable, ArtistsCategoriesTableDef}
import models.ArtistCategory

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class ArtistCategoryService @Inject() (dbService: DBService)(implicit ec: ExecutionContext) {
  import dbService._
  import dbService.api._

  def listArtistCategory: Future[Seq[ArtistCategory]] = {
    ArtistsCategoriesTable.result
      .execute()
      .map(_.map(_.toArtistsCategory))
  }

  def createArtistCategory(artist: ArtistCategory.Create): Future[ArtistCategory] = {
    val dbActions = for {
      artistCategoryCreated <- ArtistsCategoriesTable.insertWithParameters(createArtistCategoryParameters(artist))
    } yield artistCategoryCreated.toArtistsCategory
    dbActions.transactionally.execute()
  }

  def listArtistCategoryByArtistId(artistId: UUID): Future[Seq[ArtistCategory]] = {
    ArtistsCategoriesTable
      .filter(_.artist_id === artistId)
      .result
      .execute()
      .map(_.map(_.toArtistsCategory))
  }

  private def createArtistCategoryParameters(artistCategory: ArtistCategory.Create): Seq[Parameter[ArtistsCategoriesTableDef]] = Seq(
    Parameter((_: ArtistsCategoriesTableDef).artist_id, UUID.fromString(artistCategory.artist_id)),
    Parameter((_: ArtistsCategoriesTableDef).category_id, UUID.fromString(artistCategory.category_id))
  )

}
