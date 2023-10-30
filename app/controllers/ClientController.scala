package controllers

import auth.AuthAction
import models.Client
import play.api.libs.json.{__, Json, OWrites}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.ClientService
import util.{ControllerJson, EitherF}
import utils.ControllerUtil

import java.util.UUID
import javax.inject._
import scala.concurrent.ExecutionContext

@Singleton
class ClientController @Inject() (val controllerComponents: ControllerComponents, clientService: ClientService, authAction: AuthAction)(implicit
    ec: ExecutionContext
) extends BaseController
    with ControllerJson
    with ControllerUtil {

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
        clients <- EitherF.getOrElse(clientService.listClient(id), NotFound)
      } yield Ok(Json.obj("client" -> clients))
    )
  }

  def createClient: Action[Client.Create] = Action.async(parse.json[Client.Create]) { implicit request =>
    val verifyEmailIsUnique = clientService.verifyEmailIsUnique(request.body.email, None)
    EitherF.response(
      for {
        _ <- EitherF.require(verifyEmailIsUnique, BadRequest(JsonErrors(__ \ "client" \ "email", "is already registered")))
        client <- EitherF.right(clientService.createClient(request.body))
      } yield Ok(ClientResponse(Seq(client)))
    )
  }

  def updateClient(id: UUID): Action[Client.Update] = Action.async(jsonParser[Client.Update]("client")) { implicit request =>
    EitherF.response(
      for {
        clientOpt <- EitherF.right(clientService.update(id, request.body))
        client <- EitherF.getOrElse(clientOpt, NotFound)
      } yield Ok(Json.obj("client" -> client))
    )
  }

  def deleteClient(id: UUID): Action[AnyContent] = Action.async { implicit request =>
    clientService.deleteClient(id).map(optionNoContent)
  }

  private case class ClientResponse(clients: Seq[Client])
  private implicit val clientResponseWrites: OWrites[ClientResponse] = Json.writes[ClientResponse]
}
