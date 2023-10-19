package controllers

import models.SessionTattoo
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.libs.json.{Json, OWrites, __}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.SessionService
import util.{ControllerJson, EitherF}
import utils.ControllerUtil

import java.util.UUID
import scala.concurrent.ExecutionContext
import javax.inject.Inject

class SessionController @Inject() (val controllerComponents: ControllerComponents, sessionService: SessionService)(implicit
    ec: ExecutionContext
) extends BaseController
    with ControllerJson
    with ControllerUtil {

  def indexAll: Action[AnyContent] = Action.async { implicit request =>
    EitherF.response(
      for {
        sessions <- EitherF.right(sessionService.listSessions)
      } yield Ok(SessionResponse(sessions))
    )
  }

  def createSession(): Action[SessionTattoo.Create] = Action.async(jsonParser[SessionTattoo.Create]("session")) { implicit request =>
    val validateDateIncoming = sessionService.validateDateIncoming(request.body.date)
    val validateHourWorkable = sessionService.validateDateBetweenWorkableHours(request.body.date)
    EitherF.response(
      for {
        _ <- EitherF.require(validateDateIncoming, BadRequest(JsonErrors(__ \ "date", "Date must be after now")))
        _ <- EitherF.require(validateHourWorkable, BadRequest(JsonErrors(__ \ "date", "Date must be between workable hours")))
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

  def listSession(id: UUID): Action[AnyContent] = Action.async { implicit request =>
    EitherF.response(
      for {
        session <- EitherF.right(sessionService.listSession(id))
      } yield Ok(Json.obj("session" -> session))
    )
  }

  def deleteSession(id: UUID): Action[AnyContent] = Action.async { implicit request =>
    sessionService.deleteSession(id).map(optionNoContent)
  }

  private case class SessionResponse(sessions: Seq[SessionTattoo])
  private implicit val sessionResponseWrites: OWrites[SessionResponse] = Json.writes[SessionResponse]

}
