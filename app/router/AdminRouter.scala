package router

import controllers.{AdminController, ArtistController, CategoryController}
import controllers.{AdminController, AppointmentController, ArtistController, CategoryController, ClientController, SessionController}
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

import javax.inject.Inject

class AdminRouter @Inject() (
    adminController: AdminController,
    artistController: ArtistController,
    categoryController: CategoryController,
    clientController: ClientController,
    appointmentController: AppointmentController,
    sessionController: SessionController
) extends SimpleRouter {
class AdminRouter @Inject() (adminController: AdminController, artistController: ArtistController, categoryController: CategoryController)
    extends SimpleRouter {

  override def routes: Routes = {
    artistsRoutes orElse
      adminRoutes orElse
      categoriesRoutes orElse
      clientsRoutes orElse
      appointmentsRoutes orElse
      sessionRoutes
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

  private val clientsRoutes: Routes = {
    case GET(p"/clients") =>
      clientController.indexAll()
    case POST(p"/clients") =>
      clientController.addClient()
    case GET(p"/clients/$name") =>
      clientController.listClient(name)
  }

  private val appointmentsRoutes: Routes = {
    case GET(p"/appointments") =>
      appointmentController.indexAll()
    case POST(p"/appointments") =>
      appointmentController.createAppointment()
    case GET(p"/appointments/$id") =>
      appointmentController.listAppointment(id)
  }

  private val sessionRoutes: Routes = {
    case POST(p"/sessions") =>
      sessionController.createSession()
    case GET(p"/sessions") =>
      sessionController.indexAll()
  }

}
