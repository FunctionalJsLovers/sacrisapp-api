package controllers

import models.Admin

import javax.inject._
import play.api.libs.json.{Json, OWrites}
import play.api.mvc._
import services.AdminService
import util.{ControllerJson, EitherF}

import scala.concurrent.{ExecutionContext}

@Singleton
class AdminController @Inject() (val controllerComponents: ControllerComponents, adminService: AdminService)(implicit ec: ExecutionContext)
    extends BaseController
    with ControllerJson {

  def indexAll: Action[AnyContent] = Action.async { implicit request =>
    EitherF.response(
      for {
        admins <- EitherF.right(adminService.listAdmins)
      } yield Ok(AdminResponse(admins))
    )
  }

  def test: Action[AnyContent] = Action.async { implicit request =>
    EitherF.response(
      for {
        admins <- EitherF.right(adminService.listAdmins)
      } yield Ok(AdminResponse(admins))
    )
  }
  private case class AdminResponse(admins: Seq[Admin])
  private implicit val adminResponseWrites: OWrites[AdminResponse] = Json.writes[AdminResponse]

}
