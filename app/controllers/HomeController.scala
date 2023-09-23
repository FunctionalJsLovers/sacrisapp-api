package controllers

import database.Admins
import database.Admins.AdminsTable

import javax.inject._
import play.api._
import play.api.libs.json.Json
import play.api.mvc._
import slick.jdbc.PostgresProfile.api._

/** This controller creates an `Action` to handle HTTP requests to the application's home page.
  */
@Singleton
class HomeController @Inject() (val controllerComponents: ControllerComponents) extends BaseController {

  /** Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method will be called when the application receives a `GET` request with a path of `/`.
    */
  def index(): Action[AnyContent] = Action {
    Ok(views.html.index())
  }

  def index: Action[AnyContent] = Action { implicit request =>
    val admins = db.run(for {
      admin <- AdminsTable
    } yield admin).map(_.toList)


  }
  def hello(name: String): Action[AnyContent] = Action {
    Ok(views.html.hello(name))
  }

}
