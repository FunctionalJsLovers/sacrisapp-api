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

  def listClient(clientId: UUID): Future[Option[Client]] = {
    ClientsTable
      .filter(_.id === clientId)
      .result
      .headOption
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

  def deleteClient(id: UUID): Future[Option[Unit]] = {
    ClientsTable
      .filter(_.id === id)
      .delete
      .atLeastOneIsSome
      .execute()
  }

  def update(id: UUID, client: Client.Update): Future[Option[Client]] = {
    ClientsTable
      .filter(_.id === id)
      .updateReturningWithParameters(ClientsTable, updateParameters(client))
      .headOption
      .execute()
      .map(_.map(_.toClient))
  }

  def verifyEmailIsUnique(email: String, clientId: Option[UUID]): Future[Boolean] = {
    val query = ClientsTable.filterOpt(clientId)(_.id =!= _).filter(_.email === email).length
    query.result.map(_ <= 0).execute()
  }

  private def updateParameters(client: Client.Update): Seq[Parameter[ClientsTableDef]] = Seq(
    client.name.map(Parameter((_: ClientsTableDef).name, _)),
    client.phone.map(Parameter((_: ClientsTableDef).phone, _)),
    client.email.map(Parameter((_: ClientsTableDef).email, _)),
  ).flatten

  private def createClientParameters(client: Client.Create): Seq[Parameter[ClientsTableDef]] = Seq(
    Parameter((_: ClientsTableDef).name, client.name),
    Parameter((_: ClientsTableDef).phone, client.phone),
    Parameter((_: ClientsTableDef).email, client.email),
  )
}
