package database

import models.Client
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.PostgresProfile
import slick.lifted.ProvenShape

import java.util.UUID

trait Clients extends HasDatabaseConfigProvider[PostgresProfile] {
  val ClientsTable = Clients.ClientsTable

}
object Clients {
  import PostgresProfile.api._
  val ClientsTable = TableQuery[ClientsTableDef]

  class ClientsTableDef(tag: Tag) extends Table[Client](tag, "clients") {
    def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)

    def name: Rep[String] = column[String]("name")

    def phone: Rep[String] = column[String]("phone")

    def email: Rep[String] = column[String]("email")

    override def * : ProvenShape[Client] = (id, name, phone, email).<>((Client.apply _).tupled, Client.unapply)

  }
}
