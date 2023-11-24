package services

import com.google.inject.{Inject, Singleton}
import database.Appointments.AppointmentsTable
import database.Artists.{ArtistsTable, ArtistsTableDef}
import database.Sessions.SessionsTable
import models.{Appointment, Artist, SessionTattoo}

import java.time.LocalDateTime
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

  def listSessionsByArtistIdInATimeRange(artistId: UUID, startDate: LocalDateTime, endDate: LocalDateTime): Future[Seq[SessionTattoo]] = {
    val actions = for {
      query <- AppointmentsTable
                 .filter(_.artistId === artistId)
                 .join(SessionsTable)
                 .on(_.id === _.appointmentId)
                 .map { case (appointment, session) => (appointment.id, session) }
                 .filter { case (_, session) => session.date >= startDate && session.date <= endDate }
                 .result
      appointmentSessionList = query.groupMap(_._1)(_._2)
      toSession = appointmentSessionList.values.flatten.map(_.toSession).toSeq
    } yield toSession
    actions.transactionally.execute()
  }

}
