package database

import models.SessionTattoo
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.PostgresProfile
import slick.lifted.ProvenShape

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

trait Sessions extends HasDatabaseConfigProvider[PostgresProfile] {
  val SessionsTable = Sessions.SessionsTable

}

object Sessions {
  import PostgresProfile.api._
  val SessionsTable = TableQuery[SessionsTableDef]

  class SessionsTableDef(tag: Tag) extends Table[SessionRow](tag, "sessions") {
    def id: Rep[UUID] = column[UUID]("session_id", O.PrimaryKey)
    def date: Rep[LocalDateTime] = column[LocalDateTime]("date")
    def estimated_time: Rep[Int] = column[Int]("estimated_time")
    def status: Rep[String] = column[String]("status")
    def price: Rep[Int] = column[Int]("price")
    def appointmentId: Rep[UUID] = column[UUID]("appointment_id_fk")

    override def * : ProvenShape[SessionRow] =
      (id, date, estimated_time, status, price, appointmentId).mapTo[SessionRow]
  }

  case class SessionRow(
      id: UUID,
      date: LocalDateTime,
      estimated_time: Int,
      status: String,
      price: Int,
      appointmentId: UUID
  ) extends Product
      with Serializable {
    def toSession: SessionTattoo = SessionTattoo(
      id,
      date,
      estimated_time,
      status,
      price,
      appointmentId
    )
  }
}
