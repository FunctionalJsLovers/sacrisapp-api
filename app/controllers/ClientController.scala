package controllers

import auth.AuthAction
import models.Client
import play.api.libs.json.{Json, OWrites}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.ClientService
import util.{ControllerJson, EitherF}

import java.util.UUID
import javax.inject._
import scala.concurrent.ExecutionContext

@Singleton
class ClientController @Inject() (val controllerComponents: ControllerComponents, clientService: ClientService, authAction: AuthAction)(implicit
    ec: ExecutionContext
) extends BaseController
    with ControllerJson {

  def indexAll: Action[AnyContent] = Action.async { implicit request =>
    EitherF.response(
      for {
        clients <- EitherF.right(clientService.listClients)
      } yield Ok(ClientResponse(clients))
    )
  }

  def listClient(id: UUID): Action[AnyContent] = Action.async { implicit request =>
    EitherF.response(
      for {
        clients <- EitherF.right(clientService.listClient(id))
      } yield Ok(ClientResponse(clients))
    )
  }

  def addClient(): Action[Client.Create] = Action.async(parse.json[Client.Create]) { implicit request =>
    EitherF.response(
      for {
        client <- EitherF.right(clientService.createClient(request.body))
      } yield Ok(ClientResponse(Seq(client)))
    )
  }

  private case class ClientResponse(clients: Seq[Client])
  private implicit val clientResponseWrites: OWrites[ClientResponse] = Json.writes[ClientResponse]
}
