package database

import models.Admin
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.PostgresProfile
import slick.lifted.ProvenShape

import java.util.UUID

trait Admins extends HasDatabaseConfigProvider[PostgresProfile] {
  val AdminsTable = Admins.AdminsTable
}

object Admins {
  import utils.PostgresProfile.api._
  val AdminsTable = TableQuery[AdminsTableDef]

  class AdminsTableDef(tag: Tag) extends Table[AdminRow](tag, "admins") {
    def id: Rep[UUID] = column[UUID]("admin_id", O.PrimaryKey)
    def name: Rep[String] = column[String]("name")
    def phone: Rep[String] = column[String]("phone")
    def email: Rep[String] = column[String]("email")

    override def * : ProvenShape[AdminRow] = (id, name, phone, email).mapTo[AdminRow]

  }

  case class AdminRow(
      id: UUID,
      name: String,
      phone: String,
      email: String
  ) extends Product
      with Serializable {
    def toAdmin: Admin = Admin(
      id,
      name,
      phone,
      email
    )
  }

}
