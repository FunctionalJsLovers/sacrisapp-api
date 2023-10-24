package controllers

import com.google.inject.Inject
import models.{Appointment, SessionTattoo}
import play.api.libs.json.{__, Json, OWrites}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.{AppointmentService, ArtistAppointmentService, ArtistService, SessionService}
import util.{ControllerJson, EitherF}
import utils.ControllerUtil

import java.util.UUID
import scala.concurrent.ExecutionContext
import javax.inject.Singleton

@Singleton
class ArtistsAppointmentController @Inject() (
    val controllerComponents: ControllerComponents,
    appointmentService: AppointmentService,
    sessionService: SessionService,
    artistService: ArtistService,
    artistAppointmentService: ArtistAppointmentService
)(implicit
    ec: ExecutionContext
) extends BaseController
    with ControllerJson
    with ControllerUtil {

  def sessionsByArtist(artistId: UUID): Action[AnyContent] = Action.async { implicit request =>
    EitherF.response(for {
      artist <- EitherF.getOrElse(artistService.listArtist(artistId), NotFound)
      sessions <- EitherF.right(artistAppointmentService.listSessionsByArtistId(artist.id))
    } yield Ok(SessionResponse(sessions)))
  }

  private case class SessionResponse(sessions: Seq[Appointment])
  private implicit val sessionResponseWrites: OWrites[SessionResponse] = Json.writes[SessionResponse]
}
