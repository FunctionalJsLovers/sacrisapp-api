package services

import database.Appointments.{AppointmentsTable, AppointmentsTableDef}
import database.Sessions
import database.Sessions.SessionsTable
import models.{Appointment, SessionTattoo}

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AppointmentService @Inject() (dBService: DBService)(implicit ec: ExecutionContext) {
  import dBService._
  import dBService.api._

  def listAppointment(id: UUID): Future[Seq[Appointment]] = {
    AppointmentsTable
      .filter(_.id === id)
      .result
      .execute()
      .map(_.map(_.toAppointment))
  }

  def listAppointments: Future[Seq[Appointment]] = {
    AppointmentsTable.result
      .execute()
      .map(_.map(_.toAppointment))
  }

  def createAppointment(appointment: Appointment.Create): Future[Appointment] = {
    val dbActions = for {
      appointmentCreated <- AppointmentsTable.insertWithParameters(createAppointmentParameters(appointment))
    } yield appointmentCreated.toAppointment
    dbActions.transactionally.execute()
  }

  def verifyArtistAndClient(artistId: UUID, clientId: UUID): Future[Boolean] = {
    val verifyArtistAndClient = Future.successful(artistId != clientId)
    verifyArtistAndClient
  }

  def sessionsByAppointment(appointmentId: UUID): Future[Seq[SessionTattoo]] = {
    SessionsTable
      .filter(_.appointmentId === appointmentId)
      .result
      .execute()
      .map(_.map(_.toSession))
  }

  private def createAppointmentParameters(create: Appointment.Create): Seq[Parameter[AppointmentsTableDef]] = Seq(
    Parameter((_: AppointmentsTableDef).description, create.description),
    Parameter((_: AppointmentsTableDef).artistId, UUID.fromString(create.artist_id)),
    Parameter((_: AppointmentsTableDef).clientId, UUID.fromString(create.client_id)),
    Parameter((_: AppointmentsTableDef).categoryId, UUID.fromString(create.category_id))
  )

}
