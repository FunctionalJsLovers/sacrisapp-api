package services

import org.postgresql.util.PSQLException
import play.api.Configuration
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import services.DBService.DefaultQueryTimeoutSecondsConfigKey
import slick.basic.DatabasePublisher
import slick.dbio.{DBIOAction, Effect, NoStream, Streaming}
import slick.jdbc.PostgresProfile

import java.sql.SQLTimeoutException
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DBService @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, config: Configuration) extends HasDatabaseConfigProvider[PostgresProfile] {

  lazy val api = profile.api

  protected lazy val DefaultQueryTimeout: Int = config.get[Int](DefaultQueryTimeoutSecondsConfigKey)

  implicit class EnhancedDBIOAction[R, E <: Effect](action: DBIOAction[R, NoStream, E]) {
    def execute(queryTimeout: Option[Int] = Some(DefaultQueryTimeout))(implicit ec: ExecutionContext): Future[R] = {
      val actionWithTimeout = queryTimeout.fold(action) { qt =>
        new profile.JdbcActionExtensionMethods(action).withStatementParameters(statementInit = _.setQueryTimeout(qt))
      }
      db.run(actionWithTimeout).recover {
        case error: PSQLException if error.getMessage == "ERROR: canceling statement due to user request" =>
          // SP-3587: Assuming an error with this message is the result of a query hitting the specified timeout. Not sure why this isn't the default behavior.
          throw new SQLTimeoutException(error)
      }
    }

  }

  implicit class EnhancedStreamingDBIOAction[R](action: DBIOAction[Seq[R], Streaming[R], Effect.Read with Effect.Transactional]) {
    def stream(): DatabasePublisher[R] = db stream action
  }
}

object DBService {

  val DefaultQueryTimeoutSecondsConfigKey = "slick.dbs.default.queryTimeoutSeconds"
}
