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

  class ClientsTableDef(tag: Tag) extends Table[ClientRow](tag, "clients") {
    def id: Rep[UUID] = column[UUID]("client_id", O.PrimaryKey)

    def name: Rep[String] = column[String]("name")

    def phone: Rep[String] = column[String]("phone")

    def email: Rep[String] = column[String]("email")

    override def * : ProvenShape[ClientRow] = (id, name, phone, email).mapTo[ClientRow]

  }

  case class ClientRow(
      id: UUID,
      name: String,
      phone: String,
      email: String
  ) extends Product
      with Serializable {
    def toClient: Client = Client(
      id,
      name,
      phone,
      email
    )
  }
}
