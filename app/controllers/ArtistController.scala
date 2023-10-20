package controllers

import com.google.inject.{Inject, Singleton}
import models.Artist
import play.api.libs.json.{Json, OWrites}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.ArtistService
import util.{ControllerJson, EitherF}

import java.util.UUID
import scala.concurrent.ExecutionContext

@Singleton
class ArtistController @Inject() (val controllerComponents: ControllerComponents, artistService: ArtistService)(implicit ec: ExecutionContext)
    extends BaseController
    with ControllerJson {
  def indexAll: Action[AnyContent] = Action.async { implicit request =>
    EitherF.response(
      for {
        artists <- EitherF.right(artistService.listArtists)
      } yield Ok(ArtistResponse(artists))
    )
  }

  def addArtist(): Action[Artist.Create] = Action.async(parse.json[Artist.Create]) { implicit request =>
    EitherF.response(
      for {
        artist <- EitherF.right(artistService.createArtist(request.body))
      } yield Ok(ArtistResponse(Seq(artist)))
    )
  }

  def listArtist(id: UUID): Action[AnyContent] = Action.async { implicit request =>
    EitherF.response(
      for {
        artists <- EitherF.right(artistService.listArtist(id))
      } yield Ok(ArtistResponse(artists))
    )
  }

  private case class ArtistResponse(artists: Seq[Artist])
  private implicit val artistResponseWrites: OWrites[ArtistResponse] = Json.writes[ArtistResponse]
}
