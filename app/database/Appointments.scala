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

  class AppointmentsTableDef(tag: Tag) extends Table[Appointment](tag, "appointments") {
    def id: Rep[UUID] = column[UUID]("appointment_id", O.PrimaryKey)

    def description: Rep[String] = column[String]("description")

    def artistId: Rep[UUID] = column[UUID]("artist_id_fk")

    def clientId: Rep[UUID] = column[UUID]("client_id_fk")

    def categoryId: Rep[UUID] = column[UUID]("category_id_fk")

    override def * : ProvenShape[Appointment] = (id, description, artistId, clientId, categoryId)
      .<>((Appointment.apply _).tupled, Appointment.unapply)

  }
}
