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

  def topArtistsByWorkedHours: Action[AnyContent] = Action.async { implicit request =>
    EitherF.response(for {
      artists <- EitherF.right(artistAppointmentService.listTopArtistsByWorkedHours)
    } yield Ok(ArtistResponse(artists)))
  }

  private case class SessionResponse(sessions: Seq[SessionTattoo])
  private implicit val sessionResponseWrites: OWrites[SessionResponse] = Json.writes[SessionResponse]

  private case class ArtistResponse(artists: Seq[models.Artist])
  private implicit val artistResponseWrites: OWrites[ArtistResponse] = Json.writes[ArtistResponse]
}
