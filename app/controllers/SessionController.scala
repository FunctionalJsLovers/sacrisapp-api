package controllers

import models.SessionTattoo
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.libs.json.{__, Json, OWrites}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.{AppointmentService, ArtistAppointmentService, SessionService}
import util.{ControllerJson, EitherF}
import utils.ControllerUtil

import java.util.UUID
import scala.concurrent.ExecutionContext
import javax.inject.Inject

class SessionController @Inject() (
    val controllerComponents: ControllerComponents,
    sessionService: SessionService,
    artistsAppointmentService: ArtistAppointmentService,
    appointmentService: AppointmentService
)(implicit
    ec: ExecutionContext
) extends BaseController
    with ControllerJson
    with ControllerUtil {

  def indexAll: Action[AnyContent] = Action.async {
    EitherF.response(
      for {
        sessions <- EitherF.right(sessionService.listSessions)
      } yield Ok(SessionResponse(sessions))
    )
  }

  def createSession(): Action[SessionTattoo.Create] = Action.async(jsonParser[SessionTattoo.Create]("session")) { implicit request =>
    val validateDate = sessionService.validateDateIncoming(request.body.date)
    val validateSameSchedule = for {
      existingAppointments <- appointmentService.listAppointment(UUID.fromString(request.body.appointment_id))
      artistId = existingAppointments.map(_.artist_id).head
      sessions <- artistsAppointmentService.listSessionsByArtistId(artistId)
    } yield sessionService.validateSessions(sessions, request.body.date)

    EitherF.response(
      for {
        _ <- EitherF.require(
               validateSameSchedule,
               BadRequest(JsonErrors(__ \ "date" \ "hour", "Hour is already taken"))
             )
        _ <- EitherF.require(validateDate, BadRequest(JsonErrors(__ \ "date", "Date must be after now")))
        session <- EitherF.right(sessionService.createSession(request.body))
      } yield Ok(Json.obj("session" -> session))
    )
  }

  def updateSession(id: UUID): Action[SessionTattoo.Update] = Action.async(jsonParser[SessionTattoo.Update]("session")) { implicit request =>
    EitherF.response(
      for {
        sessionOpt <- EitherF.right(sessionService.update(id, request.body))
        session <- EitherF.getOrElse(sessionOpt, NotFound)
      } yield Ok(Json.obj("session" -> session))
    )
  }

  def listSession(id: UUID): Action[AnyContent] = Action.async {
    EitherF.response(
      for {
        session <- EitherF.right(sessionService.listSession(id))
      } yield Ok(Json.obj("session" -> session))
    )
  }

  def deleteSession(id: UUID): Action[AnyContent] = Action.async {
    sessionService.deleteSession(id).map(optionNoContent)
  }

  private case class SessionResponse(sessions: Seq[SessionTattoo])
  private implicit val sessionResponseWrites: OWrites[SessionResponse] = Json.writes[SessionResponse]

}
