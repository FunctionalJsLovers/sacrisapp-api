package database

import models.SessionTattoo
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.PostgresProfile
import slick.lifted.ProvenShape

import java.time.LocalDate
import java.util.UUID

trait Sessions extends HasDatabaseConfigProvider[PostgresProfile] {
  val SessionsTable = Sessions.SessionsTable

}

object Sessions {
  import PostgresProfile.api._
  val SessionsTable = TableQuery[SessionsTableDef]

  class SessionsTableDef(tag: Tag) extends Table[SessionTattoo](tag, "sessions") {
    def id: Rep[UUID] = column[UUID]("session_id", O.PrimaryKey)
    def date: Rep[LocalDate] = column[LocalDate]("date")
    def estimated_time: Rep[Int] = column[Int]("estimated_time")
    def status: Rep[String] = column[String]("status")
    def price: Rep[Int] = column[Int]("price")
    def appointmentId: Rep[UUID] = column[UUID]("appointment_id_fk")

    override def * : ProvenShape[SessionTattoo] =
      (id, date, estimated_time, status, price, appointmentId).<>((SessionTattoo.apply _).tupled, SessionTattoo.unapply)
  }
}
