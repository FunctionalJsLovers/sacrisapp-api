package services

import com.google.inject.{Inject, Singleton}
import database.Artists.{ArtistsTable, ArtistsTableDef}
import models.Artist

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ArtistService @Inject() (dbService: DBService)(implicit ec: ExecutionContext) {
  import dbService._
  import dbService.api._

  def listArtist(artistId: UUID): Future[Seq[Artist]] = {
    ArtistsTable
      .filter(_.id === artistId)
      .result
      .execute()
      .map(_.map(_.toArtist))
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

  private def createArtistParameters(artist: Artist.Create): Seq[Parameter[ArtistsTableDef]] = Seq(
    Parameter((_: ArtistsTableDef).name, artist.name),
    Parameter((_: ArtistsTableDef).phone, artist.phone),
    Parameter((_: ArtistsTableDef).email, artist.email),
    Parameter((_: ArtistsTableDef).admin_id, artist.admin_id),
    Parameter((_: ArtistsTableDef).description, artist.description),
    Parameter((_: ArtistsTableDef).instagram, artist.instagram),
    Parameter((_: ArtistsTableDef).username, artist.username)
  )

}
