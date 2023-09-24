package router

import controllers.AdminController
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._
import javax.inject.Inject

class AdminRouter @Inject() (adminController: AdminController) extends SimpleRouter {

  override def routes: Routes = {
    adminRoutes orElse
      artistsRoutes
  }

  private val adminRoutes: Routes = { case GET(p"/admins") =>
    adminController.indexAll
  }

  private val artistsRoutes: Routes = { case GET(p"/artists/") =>
    adminController.test
  }

}
