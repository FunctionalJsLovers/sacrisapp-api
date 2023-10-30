package services

import database.Appointments.{AppointmentsTable, AppointmentsTableDef}
import database.Sessions.SessionsTable
import models.{Appointment, SessionTattoo}
import play.api.mvc.Results.NotFound
import util.EitherF
import utils.PostgresProfile.PostgresAPI

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

  def createAppointment(appointment: Appointment.Create, identifier: String): Future[Appointment] = {
    val dbActions = for {
      appointmentCreated <- AppointmentsTable.insertWithParameters(createAppointmentParameters(appointment, identifier))
    } yield appointmentCreated.toAppointment
    dbActions.transactionally.execute()
  }

  def deleteAppointment(id: UUID): Future[Option[Unit]] = {
    AppointmentsTable
      .filter(_.id === id)
      .delete
      .atLeastOneIsSome
      .execute()
  }

  def update(id: UUID, appointment: Appointment.Update): Future[Option[Appointment]] = {
    AppointmentsTable
      .filter(_.id === id)
      .updateReturningWithParameters(AppointmentsTable, updateParameters(appointment))
      .headOption
      .execute()
      .map(_.map(_.toAppointment))
  }

  def verifyArtistAndClient(artistId: UUID, clientId: UUID): Future[Boolean] = {
    val verifyArtistAndClient = Future.successful(artistId != clientId)
    verifyArtistAndClient
  }

  def listAppointmentsByArtist(artistId: UUID): Future[Seq[Appointment]] = {
    AppointmentsTable
      .filter(_.artistId === artistId)
      .result
      .execute()
      .map(_.map(_.toAppointment))
  }

  private def createAppointmentParameters(create: Appointment.Create, identifier: String): Seq[Parameter[AppointmentsTableDef]] = Seq(
    Parameter((_: AppointmentsTableDef).description, create.description),
    Parameter((_: AppointmentsTableDef).artistId, UUID.fromString(create.artist_id)),
    Parameter((_: AppointmentsTableDef).clientId, UUID.fromString(create.client_id)),
    Parameter((_: AppointmentsTableDef).categoryId, UUID.fromString(create.category_id)),
    Parameter((_: AppointmentsTableDef).identifier, identifier)
  )

  private def updateParameters(appointment: Appointment.Update): Seq[Parameter[AppointmentsTableDef]] = Seq(
    appointment.description.map(Parameter((_: AppointmentsTableDef).description, _)),
  ).flatten

}
