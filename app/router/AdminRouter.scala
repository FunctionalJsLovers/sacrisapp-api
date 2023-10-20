package router

import controllers._
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

import java.util.UUID
import javax.inject.Inject

class AdminRouter @Inject() (
    adminController: AdminController,
    artistController: ArtistController,
    categoryController: CategoryController,
    clientController: ClientController,
    appointmentController: AppointmentController,
    sessionController: SessionController
) extends SimpleRouter {

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
    case GET(p"/artists/${uuid(id)}") =>
      artistController.listArtist(id)
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
    case GET(p"/clients/${uuid(id)}") =>
      clientController.listClient(id)
  }

  private val appointmentsRoutes: Routes = {
    case GET(p"/appointments") =>
      appointmentController.indexAll()
    case POST(p"/appointments") =>
      appointmentController.createAppointment()
    case GET(p"/appointments/$id") =>
      appointmentController.listAppointment(id)
    case GET(p"/appointments/${uuid(id)}/sessions") =>
      appointmentController.listSessionsByAppointment(id)
    case PATCH(p"/appointments/${uuid(id)}") =>
      appointmentController.updateAppointment(id)
    case DELETE(p"/appointments/${uuid(id)}") =>
      appointmentController.deleteAppointment(id)
  }

  private val sessionRoutes: Routes = {
    case POST(p"/sessions") =>
      sessionController.createSession()
    case GET(p"/sessions") =>
      sessionController.indexAll()
    case GET(p"/sessions/${uuid(id)}") =>
      sessionController.listSession(id)
    case PATCH(p"/sessions/${uuid(id)}") =>
      sessionController.updateSession(id)
    case DELETE(p"/sessions/${uuid(id)}") =>
      sessionController.deleteSession(id)
  }
  val uuid = new PathBindableExtractor[UUID]

}
