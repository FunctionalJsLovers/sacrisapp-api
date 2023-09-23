package database

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.PostgresProfile
import slick.lifted.ProvenShape

import java.time.Instant

trait Admins extends HasDatabaseConfigProvider[PostgresProfile] {
  val AdminsTable = Admins.AdminsTable
}

object Admins {
  import PostgresProfile.api._
  val AdminsTable = TableQuery[AdminsTableDef]

  case class AdminRow(
      name: String,
      phone: String,
      email: String,
      created_at: Instant,
      rls: RLS
  )

  class AdminsTableDef(tag: Tag) extends Table[AdminRow](tag, "admins") {
    def name: Rep[String] = column[String]("name")
    def phone: Rep[String] = column[String]("phone")
    def email: Rep[String] = column[String]("email")
    def created_at: Rep[Instant] = column[Instant]("created_at")

    override def * : ProvenShape[AdminRow] = (name, phone, email, created_at)
      .<>(AdminRow.tupled, AdminRow.unapply)
  }


  class RLS{

  }
}
