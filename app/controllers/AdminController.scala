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

  def addAdmin: Action[Admin.Create] = authAction.async(parse.json[Admin.Create]) { implicit request =>
    val body = request.body
    EitherF.response(
      for {
        admin <- EitherF.right(adminService.addAdmin(body))
      } yield Ok(AdminResponse(Seq(request.body)))
    )
  }
  private case class AdminResponse(admins: Seq[Admin])
  private implicit val adminResponseWrites: OWrites[AdminResponse] = Json.writes[AdminResponse]


}
