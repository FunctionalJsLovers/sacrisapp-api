package services

import com.google.inject.Singleton
import database.Admins.{AdminsTable, AdminsTableDef}
import models.Admin

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AdminService @Inject() (dbService: DBService)(implicit ec: ExecutionContext) {

  import dbService._
  import dbService.api._

  def listAdmin(name: String): Future[Seq[Admin]] = {
    AdminsTable
      .filter(_.name === name)
      .result
      .execute()
  }
  def listAdmins: Future[Seq[Admin]] = {
    AdminsTable.result
      .execute()
  }

  def addAdmin(admin: Admin.Create): Future[Int] = {
    val asAdmin = Admin.Create(admin.name, admin.phone, admin.email)
    val query = AdminsTable.inserw
    query.execute()
  }

  private def createAdminParameters(admin: Admin): Seq[Parameter[AdminsTableDef]] = Seq(
    Parameter((_: AdminsTableDef).name, admin.name),
    Parameter((_: AdminsTableDef).email, admin.email),
    Parameter((_: AdminsTableDef).phone, admin.phone)
  )

}
