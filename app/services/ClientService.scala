package services

import com.google.inject.Inject
import database.Clients.{ClientsTable, ClientsTableDef}
import models.Client

import java.util.UUID
import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ClientService @Inject() (dBService: DBService)(implicit ec: ExecutionContext) {
  import dBService._
  import dBService.api._

  def listClient(clientId: UUID): Future[Seq[Client]] = {
    ClientsTable
      .filter(_.id === clientId)
      .result
      .execute()
      .map(_.map(_.toClient))
  }

  def listClients: Future[Seq[Client]] = {
    ClientsTable.result
      .execute()
      .map(_.map(_.toClient))
  }

  def createClient(client: Client.Create): Future[Client] = {
    val dbActions = for {
      clientCreated <- ClientsTable.insertWithParameters(createClientParameters(client))
    } yield clientCreated.toClient
    dbActions.transactionally.execute()
  }

  private def createClientParameters(client: Client.Create): Seq[Parameter[ClientsTableDef]] = Seq(
    Parameter((_: ClientsTableDef).name, client.name),
    Parameter((_: ClientsTableDef).phone, client.phone),
    Parameter((_: ClientsTableDef).email, client.email),
  )
}
