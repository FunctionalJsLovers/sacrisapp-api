package controllers

import com.google.inject.{Inject, Singleton}
import models.Artist.ArtistMap
import models.{Artist, SessionTattoo}
import play.api.libs.json.{Json, OWrites}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.ReportService
import util.{ControllerJson, EitherF}
import utils.ControllerUtil

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext

@Singleton
class ReportController @Inject() (val controllerComponents: ControllerComponents, reportService: ReportService)(implicit ec: ExecutionContext)
    extends BaseController
    with ControllerJson
    with ControllerUtil {

  def topArtistByNumberOfSessions(): Action[AnyContent] = Action.async { implicit request =>
    val startDate = LocalDateTime.parse(request.getQueryString("startDate").getOrElse("2023-09-01T00:00:00"))
    val endDate = LocalDateTime.parse(request.getQueryString("endDate").getOrElse("2023-10-01T00:00:00"))
    EitherF.response(
      for {
        topArtist <- EitherF.right(reportService.topArtistByNumberOfSessions(startDate, endDate))
      } yield Ok(topArtist)
    )
  }

  def topArtistByWorkedHours: Action[AnyContent] = Action.async {
    EitherF.response(
      for {
        topArtist <- EitherF.right(reportService.topArtistByWorkedHours())
      } yield Ok(topArtist)
    )
  }

  def totalSalesLast30Days: Action[AnyContent] = Action.async {
    EitherF.response(
      for {
        totalSales <- EitherF.right(reportService.totalSalesLast30Days())
      } yield Ok(totalSales)
    )
  }

  /**def topCategoriesMonth: Action[AnyContent] = Action.async {
    EitherF.response(
      for {
        topCategories <- EitherF.right(reportService.topCategoriesByMonth())
      } yield Ok(topCategories)
    )
  }**/

  def topCategories: Action[AnyContent] = Action.async {
    EitherF.response(
      for {
        topCategories <- EitherF.right(reportService.topCategories())
      } yield Ok(topCategories)
    )
  }

}
