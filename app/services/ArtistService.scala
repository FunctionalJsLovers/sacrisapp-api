package services

import cats.implicits.toTraverseOps
import com.google.inject.{Inject, Singleton}
import database.Artists.{ArtistsTable, ArtistsTableDef}
import database.ArtistsCategories
import database.ArtistsCategories.ArtistsCategoriesTable
import database.Categories.CategoriesTable
import models.Artist

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ArtistService @Inject() (dbService: DBService, artistCategoryService: ArtistCategoryService, categoryService: CategoryService)(implicit
    ec: ExecutionContext
) {

  import dbService._
  import dbService.api._

  def listArtist(artistId: UUID): Future[Option[Artist]] = {
    val artist = ArtistsTable
      .filter(_.id === artistId)
      .map(a => (a.id, a.name, a.phone, a.email, a.admin_id, a.description, a.instagram, a.username))
      .result
      .headOption
    val actions = for {
      artistOpt <- artist
      artistCategoriesByArtist <- ArtistsCategoriesTable
                                    .filter(_.artist_id === artistId)
                                    .join(ArtistsTable)
                                    .on(_.artist_id === _.id)
                                    .map(_._1.category_id)
                                    .result

      artistCategories <- CategoriesTable
                            .filter(_.id inSet artistCategoriesByArtist)
                            .map(_.name)
                            .result

    } yield artistOpt.map { case (i, n, p, e, ad, d, ig, u) =>
      Artist(
        i,
        n,
        p,
        e,
        ad,
        d,
        ig,
        u,
        artistCategories
      )
    }
    actions.execute()
  }

  def listArtists: Future[Seq[Artist]] = {
    ArtistsTable.result
      .execute()
      .map(_.map(_.toArtist))
  }

  def createArtist(artist: Artist.Create): Future[Artist] = {
    val dbActions = for {
      artistCreated <- ArtistsTable.insertWithParameters(createArtistParameters(artist))
    } yield artistCreated.toArtist
    dbActions.transactionally.execute()
  }

  def deleteArtist(id: UUID): Future[Option[Unit]] = {
    ArtistsTable
      .filter(_.id === id)
      .delete
      .atLeastOneIsSome
      .execute()
  }

  def update(id: UUID, artist: Artist.Update): Future[Option[Artist]] = {
    ArtistsTable
      .filter(_.id === id)
      .updateReturningWithParameters(ArtistsTable, updateParameters(artist))
      .headOption
      .execute()
      .map(_.map(_.toArtist))
  }

  def verifyEmailIsUnique(email: String): Future[Boolean] = {
    val query = ArtistsTable.filter(_.email === email).length
    query.result.map(_ <= 0).execute()
  }

  private def createArtistParameters(artist: Artist.Create): Seq[Parameter[ArtistsTableDef]] = Seq(
    Parameter((_: ArtistsTableDef).name, artist.name),
    Parameter((_: ArtistsTableDef).phone, artist.phone),
    Parameter((_: ArtistsTableDef).email, artist.email),
    Parameter((_: ArtistsTableDef).admin_id, artist.admin_id),
    Parameter((_: ArtistsTableDef).description, artist.description),
    Parameter((_: ArtistsTableDef).instagram, artist.instagram),
    Parameter((_: ArtistsTableDef).username, artist.username)
  )

  private def updateParameters(artist: Artist.Update): Seq[Parameter[ArtistsTableDef]] = Seq(
    artist.name.map(Parameter((_: ArtistsTableDef).name, _)),
    artist.phone.map(Parameter((_: ArtistsTableDef).phone, _)),
    artist.email.map(Parameter((_: ArtistsTableDef).email, _)),
    artist.description.map(Parameter((_: ArtistsTableDef).description, _)),
    artist.instagram.map(Parameter((_: ArtistsTableDef).instagram, _)),
    artist.username.map(Parameter((_: ArtistsTableDef).username, _))
  ).flatten

}
