package database

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.PostgresProfile
import models.Product
import slick.lifted.MappedToBase.mappedToIsomorphism
import slick.lifted.ProvenShape

import java.util.UUID
trait Products extends HasDatabaseConfigProvider[PostgresProfile] {
  val ProductsTable = Products.ProductsTable

}

object Products {
  import PostgresProfile.api._
  val ProductsTable = TableQuery[ProductsTableDef]

  class ProductsTableDef(tag: Tag) extends Table[Product](tag, "products") {
    def id: Rep[UUID] = column[UUID]("product_id", O.PrimaryKey)
    def name: Rep[String] = column[String]("product_name")
    def price: Rep[Int] = column[Int]("price")
    def artistId: Rep[UUID] = column[UUID]("artist_id_fk")

    override def * : ProvenShape[Product] = (id, name, price, artistId)
      .<>((Product.apply _).tupled, Product.unapply)

  }
}
