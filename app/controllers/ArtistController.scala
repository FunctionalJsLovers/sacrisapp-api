package controllers

import com.google.inject.{Inject, Singleton}
import models.Artist
import play.api.libs.json.{__, Json, OWrites}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.ArtistService
import util.{ControllerJson, EitherF}
import utils.ControllerUtil

import java.util.UUID
import scala.concurrent.ExecutionContext

@Singleton
class ArtistController @Inject() (val controllerComponents: ControllerComponents, artistService: ArtistService)(implicit ec: ExecutionContext)
    extends BaseController
    with ControllerJson
    with ControllerUtil {
  def indexAll: Action[AnyContent] = Action.async {
    EitherF.response(
      for {
        artists <- EitherF.right(artistService.listArtists)
      } yield Ok(ArtistResponse(artists))
    )
  }

  def createArtist(): Action[Artist.Create] = Action.async(parse.json[Artist.Create]) { implicit request =>
    EitherF.response(
      for {
        _ <- EitherF.require(artistService.verifyEmailIsUnique(request.body.email), BadRequest(JsonErrors(__ \ "artist" \ "email", "is already registered")))
        artist <- EitherF.right(artistService.createArtist(request.body))
      } yield Ok(ArtistResponse(Seq(artist)))
    )
  }

  def listArtist(id: UUID): Action[AnyContent] = Action.async {
    EitherF.response(
      for {
        artist <- EitherF.getOrElse(artistService.listArtist(id), NotFound)
      } yield Ok(Json.obj("artist" -> artist))
    )
  }

  def updateArtist(id: UUID): Action[Artist.Update] = Action.async(jsonParser[Artist.Update]("artist")) { implicit request =>
    EitherF.response(
      for {
        artistOpt <- EitherF.right(artistService.update(id, request.body))
        artist <- EitherF.getOrElse(artistOpt, NotFound)
      } yield Ok(Json.obj("artist" -> artist))
    )
  }

  def deleteArtist(id: UUID): Action[AnyContent] = Action.async {
    artistService.deleteArtist(id).map(optionNoContent)
  }

  private case class ArtistResponse(artists: Seq[Artist])
  private implicit val artistResponseWrites: OWrites[ArtistResponse] = Json.writes[ArtistResponse]
}
