package controllers

import com.google.inject.{Inject, Singleton}
import models.{ArtistCategory, Category}
import play.api.libs.json.{Json, OWrites}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.{ArtistCategoryService, CategoryService}
import util.{ControllerJson, EitherF}

import scala.concurrent.ExecutionContext

@Singleton
class CategoryController @Inject() (
    val controllerComponents: ControllerComponents,
    categoryService: CategoryService,
    artistCategoryService: ArtistCategoryService
)(implicit ec: ExecutionContext)
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

  def listArtistCategories(): Action[AnyContent] = Action.async { implicit request =>
    EitherF.response(
      for {
        artistCategories <- EitherF.right(artistCategoryService.listArtistCategory)
      } yield Ok(ArtistCategoryResponse(artistCategories))
    )
  }

  def createArtistCategory(): Action[ArtistCategory.Create] = Action.async(parse.json[ArtistCategory.Create]) { implicit request =>
    EitherF.response(
      for {
        artistCategory <- EitherF.right(artistCategoryService.createArtistCategory(request.body))
      } yield Ok(ArtistCategoryResponse(Seq(artistCategory)))
    )
  }

  private case class CategoryResponse(categories: Seq[Category])
  private implicit val categoryResponseWrites: OWrites[CategoryResponse] = Json.writes[CategoryResponse]

  private case class ArtistCategoryResponse(artistCategories: Seq[ArtistCategory])
  private implicit val artistCategoryResponseWrites: OWrites[ArtistCategoryResponse] = Json.writes[ArtistCategoryResponse]
}
