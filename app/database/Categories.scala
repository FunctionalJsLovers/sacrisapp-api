package database

import models.Category
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.PostgresProfile
import slick.lifted.ProvenShape

import java.util.UUID

trait Categories extends HasDatabaseConfigProvider[PostgresProfile] {
  val CategoriesTable = Categories.CategoriesTable

}

object Categories {
  import utils.PostgresProfile.api._
  val CategoriesTable = TableQuery[CategoriesTableDef]

  class CategoriesTableDef(tag: Tag) extends Table[CategoryRow](tag, "categories") {
    def id: Rep[UUID] = column[UUID]("category_id", O.PrimaryKey)
    def name: Rep[String] = column[String]("name")

    override def * : ProvenShape[CategoryRow] = (id, name).mapTo[CategoryRow]

  }

  case class CategoryRow(
      id: UUID,
      name: String
  ) extends Product
      with Serializable {
    def toCategory: Category = Category(
      id,
      name
    )
  }
}
