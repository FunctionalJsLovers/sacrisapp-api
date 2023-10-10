package controllers

import auth.AuthAction
import models.Admin

import javax.inject._
import play.api.libs.json.{Json, OWrites}
import play.api.mvc._
import services.AdminService
import util.{ControllerJson, EitherF}

import scala.concurrent.ExecutionContext

@Singleton
class AdminController @Inject() (val controllerComponents: ControllerComponents, adminService: AdminService, authAction: AuthAction)(implicit
    ec: ExecutionContext
) extends BaseController
    with ControllerJson {

  def indexAll: Action[AnyContent] = authAction.async { implicit request =>
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

  def addAdmin(): Action[Admin.Create] = Action.async(parse.json[Admin.Create]) { implicit request =>
    EitherF.response(
      for {
        admin <- EitherF.right(adminService.createAdmin(request.body))
      } yield Ok(AdminResponse(Seq(admin)))
    )
  }
  private case class AdminResponse(admins: Seq[Admin])
  private implicit val adminResponseWrites: OWrites[AdminResponse] = Json.writes[AdminResponse]

}
