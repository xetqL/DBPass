package DBPass

import Model.{DAL}
import DBPass.Services._
import akka.actor._
import akka.io.IO
import org.slf4j.LoggerFactory
import slick.dbio.DBIO
import slick.driver.{H2Driver, SQLiteDriver, PostgresDriver}
import slick.jdbc.meta.MTable
import slick.jdbc.JdbcBackend.Database
import spray.can._
import scala.concurrent.{ Await, Promise}
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import util.Util.unloadDrivers
case class DatabaseCreationException(cause: Throwable) extends Exception

object Boot extends App {
    def logger = LoggerFactory.getLogger(this.getClass)
    def run(dal: DAL, db: Database): Unit = {
        val setupAction = DBIO.seq()
        val promiseOnSetupHTTPServer = Promise[Boolean]
        val f = db.run(MTable.getTables).flatMap{
            tables =>
                if(tables.map(_.name.name).count(name => Set("Users", "AuthTokens", "Keys") contains name) != 3){
                    setupAction.andThen(dal.drop).andThen(dal.create)
                }
                db.run(setupAction)
        }
        f.onComplete {
            case Failure(error) =>
                promiseOnSetupHTTPServer success false
            case Success(_) =>
                implicit val system = ActorSystem()
                // the handler actor replies to incoming HttpRequests
                val handler = system.actorOf(Props(new DBService(db, dal)), name = "handler")
                IO(Http) ! Http.Bind(handler, interface = "localhost", port = 8080)
                promiseOnSetupHTTPServer success true
        }
        Await.result(promiseOnSetupHTTPServer.future, Duration.Inf)
        promiseOnSetupHTTPServer.future.value match {
            case Some( Success(false) ) => logger.error("Database hasn't been set up.")
            case _ => logger.info("Database set up correctly.")
        }
    }
    val productionDatabaseConfigName = "test"
    try {
        //run(new DAL(PostgresDriver), Database.forConfig(productionDatabaseConfigName))
        run(new DAL(H2Driver), Database.forURL("jdbc:h2:mem:", driver="org.h2.Driver"))

    } finally unloadDrivers
}
