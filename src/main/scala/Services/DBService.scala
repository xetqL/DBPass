package DBPass.Services


import DBPass.Services.Action.Shutdown
import Model.DAL
import Services.{LoginResponseComponent, ActionResponseComponent, Response}
import akka.actor.{ActorContext, Actor, ActorLogging}
import spray.json.{JsString, JsObject}
import slick.jdbc.JdbcBackend.Database
import spray.routing.authentication.{BasicAuth, UserPass}
import scala.concurrent.duration._
import spray.routing._
import spray.util._
import spray.http._
import Model.User
import util.Util.userPassAuth
/**
 * Created by aboul on 05.08.2015.
 */

trait IDBService extends HttpService {
    implicit val db: Database
    implicit val dal: DAL

    implicit def executionContext = actorRefFactory.dispatcher

    lazy val loginRoute = {
        path("login") {
            get {
                complete {
                    <html>
                        <body>
                            <form action="/signup" method="post">
                                <input type="input" value="testvalues" id="entry" name="hellodude"/>
                                <input type="hidden" value="hiddenvalues" id="caca" name="hiddenpute"/>
                                <input type="submit" value="Send away!" id="submit"/>
                            </form>
                        </body>
                    </html>
                }
            }
        }
    }

    lazy val shutdownRoute = {
        path("shutdown") {
            get {
                parameterMap {
                    p => {
                        complete(s"${if (p.isEmpty) "no parameter" else p.map(_._2).head}")
                    }
                }
            }
        }
    }

    lazy val signupRoute = {
        path("signup") {
            post {
                authenticate(BasicAuth(userPassAuth _, realm = "secure site")) { user =>
                    complete {
                        /*in(2.second) {
                            db.close()
                            actorSystem.shutdown()
                        }*/
                        val generatedToken = "hello"
                        new Response(Status.Accepted(), "message successfully logged in") with LoginResponseComponent {
                            val token = generatedToken
                        }.toJSON.prettyPrint
                        /*HttpResponse(StatusCodes.OK, new Response(Status.Accepted(), "Shutdown scheduled") with ActionResponseComponent {
                            val action = Shutdown()
                        }.toJSON.prettyPrint)*/
                    }
                }
            }
        }
    }
    lazy val magicRoute = {
        path("kill") {
            get {
                complete {
                    in(2.second) {
                        db.close()
                        actorSystem.shutdown()
                    }
                    "kill in 2s"
                }
            }
        }
    }
    def in[U](duration: FiniteDuration)(body: => U): Unit =
        actorSystem.scheduler.scheduleOnce(duration)(body)


}

class DBService(val db: Database, val dal: DAL) extends Actor with IDBService {

    def actorRefFactory: ActorContext = context

    def receive: Receive = {
        runRoute(loginRoute ~ shutdownRoute ~ signupRoute ~ magicRoute)
    }
}


