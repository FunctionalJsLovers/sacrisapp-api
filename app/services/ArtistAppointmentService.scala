package services

import com.google.inject.{Inject, Singleton}
import database.Appointments.AppointmentsTable
import database.Artists.{ArtistsTable, ArtistsTableDef}
import database.Sessions.SessionsTable
import models.{Appointment, Artist, SessionTattoo}

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ArtistAppointmentService @Inject() (
    dbService: DBService,
    sessionService: SessionService,
    artistService: ArtistService,
    appointmentService: AppointmentService
)(implicit ec: ExecutionContext) {

  import dbService._
  import dbService.api._

  def listSessionsByArtistId(artistId: UUID): Future[Seq[SessionTattoo]] = {
    val actions = for {
      query <- AppointmentsTable
                 .filter(_.artistId === artistId)
                 .join(SessionsTable)
                 .on(_.id === _.appointmentId)
                 .map { case (appointment, session) => (appointment.id, session) }
                 .result
      appointmentSessionList = query.groupMap(_._1)(_._2)
      toSession = appointmentSessionList.values.flatten.map(_.toSession).toSeq
    } yield toSession
    actions.transactionally.execute()
  }

  def listTopArtistsByWorkedHours: Future[Seq[Artist]] = {
    val actions = for {
      query <- (for {
        ((artist, appointment), session) <- ArtistsTable
          .join(AppointmentsTable)
          .on(_.id === _.artistId)
          .join(SessionsTable)
          .on(_._2.id === _.appointmentId)
      } yield (artist, session)).result
      artistSessionList = query.groupBy(_._1).map { case (artist, tuples) => (artist, tuples.map(_._2)) }
      toArtist = artistSessionList.map {
        case (artist, sessions) =>
          val totalWorkedHours = sessions.map(_.estimated_time).sum
          Artist(artist.id, artist.name, totalWorkedHours)
      }.toSeq
    } yield toArtist
    actions.transactionally.execute()
  }



}
