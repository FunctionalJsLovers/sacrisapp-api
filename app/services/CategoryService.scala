package services

import com.google.inject.{Inject, Singleton}
import database.ArtistsCategories.ArtistsCategoriesTable
import database.Categories.{CategoriesTable, CategoriesTableDef}
import models.Category

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CategoryService @Inject() (dbService: DBService)(implicit ec: ExecutionContext) {
  import dbService._
  import dbService.api._

  def listCategories: Future[Seq[Category]] = {
    CategoriesTable.result
      .execute()
      .map(_.map(_.toCategory))
  }

  def createCategory(category: Category.Create): Future[Category] = {
    val dbActions = for {
      categoryCreated <- CategoriesTable.insertWithParameters(createCategoryParameters(category))
    } yield categoryCreated.toCategory
    dbActions.transactionally.execute()
  }

  def listCategoryByCategoryId(categoryId: UUID): Future[Category] = {
    CategoriesTable
      .filter(_.id === categoryId)
      .result
      .head
      .execute()
      .map(_.toCategory)

  }

  def listCategoriesByIds(categoriesIds: Seq[UUID]): Future[Seq[Category]] = {
    println("category ids_> " + categoriesIds)
    CategoriesTable
      .filter(_.id inSet categoriesIds)
      .result
      .execute()
      .map(_.map(_.toCategory))
  }

  private def createCategoryParameters(category: Category.Create): Seq[Parameter[CategoriesTableDef]] = Seq(
    Parameter((_: CategoriesTableDef).name, category.name),
  )

}
