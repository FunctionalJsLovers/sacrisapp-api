package controllers

import models.SessionTattoo
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.libs.json.{__, Json, OWrites}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.SessionService
import util.{ControllerJson, EitherF}

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext
import javax.inject.Inject

class SessionController @Inject() (val controllerComponents: ControllerComponents, sessionService: SessionService)(implicit
    ec: ExecutionContext
) extends BaseController
    with ControllerJson {

  def indexAll: Action[AnyContent] = Action.async { implicit request =>
    EitherF.response(
      for {
        sessions <- EitherF.right(sessionService.listSessions)
      } yield Ok(SessionResponse(sessions))
    )
  }

  def createSession(): Action[SessionTattoo.Create] = Action.async(parse.json[SessionTattoo.Create]) { implicit request =>
    println(request.body.date)
    val validateDateIncoming = sessionService.validateDateIncoming(request.body.date)
    val validateHourWorkable = sessionService.validateDateBetweenWorkableHours(request.body.date)
    EitherF.response(
      for {
        _ <- EitherF.require(validateDateIncoming, BadRequest(JsonErrors(__ \ "date", "Date must be after now")))
        _ <- EitherF.require(validateHourWorkable, BadRequest(JsonErrors(__ \ "date", "Date must be between workable hours")))
        session <- EitherF.right(sessionService.createSession(request.body))
      } yield Ok(SessionResponse(Seq(session)))
    )
  }

  private case class SessionResponse(sessions: Seq[SessionTattoo])
  private implicit val sessionResponseWrites: OWrites[SessionResponse] = Json.writes[SessionResponse]
}
