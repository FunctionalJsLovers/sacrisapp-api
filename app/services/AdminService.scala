package services

import com.google.inject.Singleton
import database.Admins.AdminsTable
import models.Admin

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AdminService @Inject() (dbService: DBService)(implicit ec: ExecutionContext) {
  import dbService._, api._

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
}
