package router

import controllers.{AdminController, ArtistController, CategoryController}
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

import javax.inject.Inject

class AdminRouter @Inject() (adminController: AdminController, artistController: ArtistController, categoryController: CategoryController)
    extends SimpleRouter {

  override def routes: Routes = {
    artistsRoutes orElse
      adminRoutes orElse
      categoriesRoutes
  }
  private val adminRoutes: Routes = {
    case GET(p"/health") =>
      adminController.test
    case GET(p"/admins") =>
      adminController.indexAll()
    case POST(p"/admins") =>
      adminController.addAdmin()
    case GET(p"/admins/$name") =>
      adminController.listAdmin(name)
  }

  private val artistsRoutes: Routes = {
    case GET(p"/artists") =>
      artistController.indexAll()
    case POST(p"/artists") =>
      artistController.addArtist()
    case GET(p"/artists/$name") =>
      artistController.listArtist(name)
  }

  private val categoriesRoutes: Routes = {
    case GET(p"/categories") =>
      categoryController.indexAll()
    case POST(p"/categories") =>
      categoryController.addCategory()
    case POST(p"/artistCategories") =>
      categoryController.createArtistCategory()
    case GET(p"/artistCategories") =>
      categoryController.listArtistCategories()
  }

}
