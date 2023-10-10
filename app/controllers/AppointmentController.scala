package controllers

import com.google.inject.Inject
import models.Appointment
import play.api.libs.json.{__, Json, OWrites}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.AppointmentService
import util.{ControllerJson, EitherF}

import java.util.UUID
import scala.concurrent.ExecutionContext
import javax.inject.Singleton

@Singleton
class AppointmentController @Inject() (val controllerComponents: ControllerComponents, appointmentService: AppointmentService)(implicit
    ec: ExecutionContext
) extends BaseController
    with ControllerJson {

  def indexAll: Action[AnyContent] = Action.async { implicit request =>
    EitherF.response(for {
      appointments <- EitherF.right(appointmentService.listAppointments)
    } yield Ok(AppointmentResponse(appointments)))

  }

  def listAppointment(id: String): Action[AnyContent] = Action.async { implicit request =>
    EitherF.response(for {
      appointments <- EitherF.right(appointmentService.listAppointment(UUID.fromString(id)))
    } yield Ok(AppointmentResponse(appointments)))
  }

  def createAppointment(): Action[Appointment.Create] = Action.async(parse.json[Appointment.Create]) { implicit request =>
    val verifyArtistAndClient = appointmentService.verifyArtistAndClient(UUID.fromString(request.body.artist_id), UUID.fromString(request.body.client_id))

    EitherF.response(for {
      _ <- EitherF.require(verifyArtistAndClient, BadRequest(JsonErrors(__ \ "artistId", "Artist and client must be different")))
      appointment <- EitherF.right(appointmentService.createAppointment(request.body))
    } yield Ok(AppointmentResponse(Seq(appointment))))
  }

  private case class AppointmentResponse(appointments: Seq[Appointment])
  private implicit val appointmentResponseWrites: OWrites[AppointmentResponse] = Json.writes[AppointmentResponse]
}
