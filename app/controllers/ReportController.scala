package controllers

import com.google.inject.{Inject, Singleton}
import models.Artist.ArtistSessionsMonth
import models.{Artist, SessionTattoo}
import play.api.libs.json.{Json, OWrites}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.ReportService
import util.{ControllerJson, EitherF}
import utils.ControllerUtil

import scala.concurrent.ExecutionContext

@Singleton
class ReportController @Inject() (val controllerComponents: ControllerComponents, reportService: ReportService)(implicit ec: ExecutionContext)
    extends BaseController
    with ControllerJson
    with ControllerUtil {

  def topArtistByNumberOfSessions: Action[AnyContent] = Action.async {
    EitherF.response(
      for {
        topArtist <- EitherF.right(reportService.topArtistByNumberOfSessions())
      } yield Ok(topArtist)
    )
  }

  def topArtistByWorkedHours: Action[AnyContent] = Action.async {
    EitherF.response(
      for {
        topArtist <- EitherF.right(reportService.topArtistByWorkedHours())
      } yield Ok(topArtist.toString)
    )
  }

}
