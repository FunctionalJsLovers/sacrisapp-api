package controllers

import com.google.inject.Inject
import models.{Appointment, SessionTattoo}
import play.api.libs.json.{__, Json, OWrites}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.{AppointmentService, ArtistService, ClientService, SessionService}
import util.{ControllerJson, EitherF}
import utils.ControllerUtil

import java.util.UUID
import scala.concurrent.ExecutionContext
import javax.inject.Singleton

@Singleton
class AppointmentController @Inject() (
    val controllerComponents: ControllerComponents,
    appointmentService: AppointmentService,
    sessionService: SessionService,
    artistService: ArtistService,
    clientService: ClientService
)(implicit
    ec: ExecutionContext
) extends BaseController
    with ControllerJson
    with ControllerUtil {

  def indexAll: Action[AnyContent] = Action.async { implicit request =>
    EitherF.response(for {
      appointments <- EitherF.right(appointmentService.listAppointments)
    } yield Ok(AppointmentResponse(appointments)))

  }

  def listAppointment(id: UUID): Action[AnyContent] = Action.async { implicit request =>
    EitherF.response(for {
      appointments <- EitherF.right(appointmentService.listAppointment(id))
    } yield Ok(AppointmentResponse(appointments)))
  }

  def createAppointment(): Action[Appointment.Create] = Action.async(parse.json[Appointment.Create]) { implicit request =>
    val verifyArtistAndClient = appointmentService.verifyArtistAndClient(UUID.fromString(request.body.artist_id), UUID.fromString(request.body.client_id))
    EitherF.response(for {
      artist <- EitherF.getOrElse(artistService.listArtist(UUID.fromString(request.body.artist_id)), NotFound)
      client <- EitherF.getOrElse(clientService.listClient(UUID.fromString(request.body.client_id)), NotFound)
      _ <- EitherF.require(verifyArtistAndClient, BadRequest(JsonErrors(__ \ "artist_id", "Artist and client must be different")))
      appointment <- EitherF.right(appointmentService.createAppointment(request.body, artist.name.concat('|' + client.name)))
    } yield Ok(AppointmentResponse(Seq(appointment))))
  }
  def updateAppointment(id: UUID): Action[Appointment.Update] = Action.async(jsonParser[Appointment.Update]("appointment")) { implicit request =>
    EitherF.response(for {
      appointmentOpt <- EitherF.right(appointmentService.update(id, request.body))
      appointment <- EitherF.getOrElse(appointmentOpt, NotFound)
    } yield Ok(Json.obj("appointment" -> appointment)))
  }

  def listSessionsByAppointment(appointmentId: UUID): Action[AnyContent] = Action.async { implicit request =>
    EitherF.response(for {
      sessions <- EitherF.right(sessionService.sessionsByAppointment(appointmentId))
    } yield Ok(SessionsByAppointmentResponse(sessions)))
  }

  def deleteAppointment(id: UUID): Action[AnyContent] = Action.async { implicit request =>
    appointmentService.deleteAppointment(id).map(optionNoContent)
  }

  private case class AppointmentResponse(appointments: Seq[Appointment])
  private implicit val appointmentResponseWrites: OWrites[AppointmentResponse] = Json.writes[AppointmentResponse]

  private case class SessionsByAppointmentResponse(sessions: Seq[SessionTattoo])
  private implicit val sessionsByAppointmentResponseWrites: OWrites[SessionsByAppointmentResponse] = Json.writes[SessionsByAppointmentResponse]

}
