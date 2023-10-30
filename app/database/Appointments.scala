package database

import models.Appointment
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.PostgresProfile
import slick.lifted.ProvenShape

import java.util.UUID

trait Appointments extends HasDatabaseConfigProvider[PostgresProfile] {
  val AppointmentsTable = Appointments.AppointmentsTable

}

object Appointments {

  import PostgresProfile.api._
  val AppointmentsTable = TableQuery[AppointmentsTableDef]

  class AppointmentsTableDef(tag: Tag) extends Table[AppointmentRow](tag, "appointments") {
    def id: Rep[UUID] = column[UUID]("appointment_id", O.PrimaryKey)

    def description: Rep[String] = column[String]("description")

    def artistId: Rep[UUID] = column[UUID]("artist_id_fk")

    def clientId: Rep[UUID] = column[UUID]("client_id_fk")

    def categoryId: Rep[UUID] = column[UUID]("artist_category_id")

    def identifier: Rep[String] = column[String]("identifier")

    override def * : ProvenShape[AppointmentRow] = (id, description, artistId, clientId, categoryId, identifier)
      .mapTo[AppointmentRow]

  }

  case class AppointmentRow(
      id: UUID,
      description: String,
      artistId: UUID,
      clientId: UUID,
      categoryId: UUID,
      identifier: String
  ) extends Product
      with Serializable {
    def toAppointment: Appointment = Appointment(
      id,
      description,
      artistId,
      clientId,
      categoryId,
      identifier = identifier
    )
  }
}
