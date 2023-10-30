package services

import com.google.inject.Singleton
import database.Sessions.{SessionsTable, SessionsTableDef}
import models.SessionTattoo

import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionService @Inject() (dbService: DBService)(implicit ec: ExecutionContext) {

  import dbService._
  import dbService.api._

  def listSession(id: UUID): Future[Seq[SessionTattoo]] = {
    SessionsTable
      .filter(_.id === id)
      .result
      .execute()
      .map(_.map(_.toSession))
  }

  def listSessions: Future[Seq[SessionTattoo]] = {
    SessionsTable.result
      .execute()
      .map(_.map(_.toSession))
  }

  def createSession(session: SessionTattoo.Create): Future[SessionTattoo] = {
    val dbActions = for {
      sessionCreated <- SessionsTable.insertWithParameters(createSessionParameters(session))
    } yield sessionCreated.toSession
    dbActions.transactionally.execute()
  }

  def deleteSession(id: UUID): Future[Option[Unit]] = {
    SessionsTable
      .filter(_.id === id)
      .delete
      .atLeastOneIsSome
      .execute()
  }
  def validateDateIncoming(date: LocalDateTime): Future[Boolean] = {
    val validateDateIncoming = Future.successful(date.isAfter(LocalDateTime.now()))
    validateDateIncoming
  }

  def validateDateBetweenWorkableHours(date: LocalDateTime): Future[Boolean] = {
    val dateHour = date.getHour
    val validateDateBetweenWorkableHours = Future.successful(dateHour >= 9 && dateHour <= 23)
    validateDateBetweenWorkableHours
  }

  def update(id: UUID, session: SessionTattoo.Update): Future[Option[SessionTattoo]] = {
    SessionsTable
      .filter(_.id === id)
      .updateReturningWithParameters(SessionsTable, updateParameters(session))
      .headOption
      .execute()
      .map(_.map(_.toSession))
  }

  def sessionsByAppointment(appointmentId: UUID): Future[Seq[SessionTattoo]] = {
    SessionsTable
      .filter(_.appointmentId === appointmentId)
      .result
      .execute()
      .map(_.map(_.toSession))
  }

  def validateSessions(sessions: Seq[SessionTattoo], date: LocalDateTime): Boolean = {
    val sameDaySessions = sessions.filter(_.date.toLocalDate.equals(date.toLocalDate)).map(_.date)
    val sameDaySessionsHour = sameDaySessions.map(_.getHour)
    val dateHour = date.getHour
    !sameDaySessionsHour.contains(dateHour)
  }

  private def createSessionParameters(session: SessionTattoo.Create): Seq[Parameter[SessionsTableDef]] = Seq(
    Parameter((_: SessionsTableDef).date, session.date),
    Parameter((_: SessionsTableDef).estimated_time, session.estimated_time),
    Parameter((_: SessionsTableDef).status, session.status),
    Parameter((_: SessionsTableDef).price, session.price),
    Parameter((_: SessionsTableDef).appointmentId, UUID.fromString(session.appointment_id))
  )

  private def updateParameters(sessionTattoo: SessionTattoo.Update): Seq[Parameter[SessionsTableDef]] = Seq(
    sessionTattoo.date.map(Parameter((_: SessionsTableDef).date, _)),
    sessionTattoo.estimated_time.map(Parameter((_: SessionsTableDef).estimated_time, _)),
    sessionTattoo.status.map(Parameter((_: SessionsTableDef).status, _)),
    sessionTattoo.price.map(Parameter((_: SessionsTableDef).price, _))
  ).flatten

}
