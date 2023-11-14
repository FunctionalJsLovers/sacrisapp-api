package services

import com.google.inject.Inject
import models.Artist

import scala.concurrent.{ExecutionContext, Future}

class ReportService @Inject() (dbService: DBService, artistAppointmentService: ArtistAppointmentService, artistService: ArtistService)(implicit
    ec: ExecutionContext
) {

  import dbService._
  import dbService.api._

  def topArtistByNumberOfSessions(): Any = {
    for {
      artists <- artistService.listArtists
      artistsWithSessions <-
        Future.sequence(artists.map(artist => artistAppointmentService.listSessionsByArtistId(artist.id).map(sessions => (artist, sessions))))

    } yield artistsWithSessions
  }
}
