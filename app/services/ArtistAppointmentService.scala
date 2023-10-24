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

  def listSessionsByArtistId(artistId: UUID): Future[Seq[Appointment]] = {
    val appointments = AppointmentsTable
      .filter(_.artistId === artistId)
      .map(a => (a.id, a.description, a.artistId, a.clientId, a.categoryId))
      .result

    val actions = for {
      appointmentOpt <- appointments
      query <- AppointmentsTable
                 .filter(_.artistId inSet appointmentOpt.map(_._3))
                 .join(SessionsTable)
                 .on(_.id === _.appointmentId)
                 .map { case (appointment, session) => (appointment.id, session) }
                 .result
      appointmentSessionList = query.groupMap(_._1)(_._2)
      toSession = appointmentSessionList.values.flatten.map(_.toSession).toSeq
    } yield appointmentOpt.map { case (i, d, ai, ci, catI) =>
      Appointment(
        i,
        d,
        ai,
        ci,
        catI,
        sessions = Some(toSession)
      )
    }
    actions.transactionally.execute()
  }

}
