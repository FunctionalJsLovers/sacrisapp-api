package services

import com.google.inject.Singleton
import database.Sessions
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

  def listSession(date: LocalDateTime): Future[Seq[SessionTattoo]] = {
    SessionsTable
      .filter(_.date === date)
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
  def validateDateIncoming(date: LocalDateTime): Future[Boolean] = {
    val validateDateIncoming = Future.successful(date.isAfter(LocalDateTime.now()))
    validateDateIncoming
  }

  def validateDateBetweenWorkableHours(date: LocalDateTime): Future[Boolean] = {
    val dateHour = date.getHour
    val validateDateBetweenWorkableHours = Future.successful(dateHour >= 9 && dateHour <= 23)
    validateDateBetweenWorkableHours
  }
  private def createSessionParameters(session: SessionTattoo.Create): Seq[Parameter[SessionsTableDef]] = Seq(
    Parameter((_: SessionsTableDef).date, session.date),
    Parameter((_: SessionsTableDef).estimated_time, session.estimated_time),
    Parameter((_: SessionsTableDef).status, session.status),
    Parameter((_: SessionsTableDef).price, session.price),
    Parameter((_: SessionsTableDef).appointmentId, UUID.fromString(session.appointmentId))
  )

}
