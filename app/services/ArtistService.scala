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

  def listArtist(artistId: UUID): Future[Option[Artist]] = {
    ArtistsTable
      .filter(_.id === artistId)
      .result
      .headOption
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
