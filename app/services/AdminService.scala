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
      .map(_.map(_.toAdmin))
  }
  def listAdmins: Future[Seq[Admin]] = {
    AdminsTable.result
      .execute()
      .map(_.map(_.toAdmin))
  }

  def createAdmin(admin: Admin.Create): Future[Admin] = {
    val dbActions = for {
      adminCreated <- AdminsTable.insertWithParameters(createAdminParameters(admin))
    } yield adminCreated.toAdmin
    dbActions.transactionally.execute()
  }

  private def createAdminParameters(admin: Admin.Create): Seq[Parameter[AdminsTableDef]] = Seq(
    Parameter((_: AdminsTableDef).name, admin.name),
    Parameter((_: AdminsTableDef).email, admin.email),
    Parameter((_: AdminsTableDef).phone, admin.phone)
  )

}
