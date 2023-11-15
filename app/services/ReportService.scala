package services

import com.google.inject.Inject
import models.Artist.ArtistMap
import models.{Artist, SessionTattoo}
import models.Artist

import scala.concurrent.{ExecutionContext, Future}

class ReportService @Inject() (dbService: DBService, artistAppointmentService: ArtistAppointmentService, artistService: ArtistService)(implicit
    ec: ExecutionContext
) {

  import dbService._
  import dbService.api._

  def topArtistByNumberOfSessions(): Future[ArtistMap] = {
    for {
      artists <- artistService.listArtists
      artistsWithSessions <-
        Future.sequence(artists.map(artist => artistAppointmentService.listSessionsByArtistId(artist.id).map(sessions => (artist, sessions))))
      countSessionByArtist = artistsWithSessions.map(artistWithSession => (artistWithSession._1, artistWithSession._2.size)).toMap
      artistNameSession = countSessionByArtist.map(artistSession => (artistSession._1.name, artistSession._2))
    } yield ArtistMap(artistNameSession)
  }

  def topArtistByWorkedHours(): Future[ArtistMap] = {
    for {
      artists <- artistService.listArtists
      artistsWithHours <-
        Future.sequence(
          artists.map(artist => artistAppointmentService.listSessionsByArtistId(artist.id).map(session => (artist, session.map(_.estimated_time).sum)))
        )
      countArtistHourMap = artistsWithHours.map(artistsWithHours => (artistsWithHours._1.name, artistsWithHours._2)).toMap
    } yield ArtistMap(countArtistHourMap)
  }
}
