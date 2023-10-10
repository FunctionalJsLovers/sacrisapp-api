package controllers

import com.google.inject.Inject
import models.Category
import play.api.libs.json.{Json, OWrites}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.CategoryService
import util.{ControllerJson, EitherF}

import scala.concurrent.ExecutionContext

class CategoryController @Inject() (val controllerComponents: ControllerComponents, categoryService: CategoryService)(implicit ec: ExecutionContext)
    extends BaseController
    with ControllerJson {
  def indexAll: Action[AnyContent] = Action.async { implicit request =>
    EitherF.response(
      for {
        categories <- EitherF.right(categoryService.listCategories)
      } yield Ok(CategoryResponse(categories))
    )
  }

  def addCategory(): Action[Category.Create] = Action.async(parse.json[Category.Create]) { implicit request =>
    EitherF.response(
      for {
        category <- EitherF.right(categoryService.createCategory(request.body))
      } yield Ok(CategoryResponse(Seq(category)))
    )
  }

  private case class CategoryResponse(categories: Seq[Category])
  private implicit val categoryResponseWrites: OWrites[CategoryResponse] = Json.writes[CategoryResponse]
}
