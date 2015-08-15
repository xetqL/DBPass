package DBPass.Services


import java.lang.Exception

import DBPass.Services.Action.Shutdown
import Model.DAL
import Services.{LoginResponseComponent, ActionResponseComponent, Response}
import akka.actor.{ActorContext, Actor, ActorLogging}
import spray.httpx.marshalling.ToResponseMarshallable
import spray.json.{JsString, JsObject}
import slick.jdbc.JdbcBackend.Database
import spray.routing.authentication.{BasicAuth, UserPass}
import scala.concurrent.{Await, Promise}
import scala.concurrent.duration._
import spray.routing._
import spray.util._
import spray.http._
import Model.{User, Token}
import util.Util.userPassAuth
import scala.util.Success
import scala.util.control.Exception

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
                        complete {
                            val pResponse = Promise[HttpResponse]
                            lazy val badRequest =
                                HttpResponse(StatusCodes.BadRequest, new Response(Status.Rejected(), "bad username : token provided") with ActionResponseComponent {
                                    val action = Shutdown()
                                }.toJSON.prettyPrint)
                            lazy val goodRequest =
                                HttpResponse(StatusCodes.OK, new Response(Status.Accepted(), "Shutdown scheduled") with ActionResponseComponent {
                                    val action = Shutdown()
                                }.toJSON.prettyPrint)
                            try {
                                val token = p("token")
                                val retrieve = db.run(dal.get(p("username"))).flatMap {
                                    case Some(user) => db.run(dal.get(user.userID, token))
                                    case _ => throw new Exception()
                                }
                                val r = retrieve onComplete {
                                    case Success(Some(_)) => {
                                        in(2.second) {
                                            db.close()
                                            actorSystem.shutdown()
                                        }
                                        pResponse success goodRequest
                                    }
                                    case _ => throw new Exception()
                                }
                                Await.result(pResponse.future, Duration.Inf)
                                pResponse.future.value match {
                                    case Some(r) => r
                                    case _ => badRequest
                                }
                            } catch {
                                case _ => badRequest
                            }
                        }
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
                        lazy val badRequest =
                            HttpResponse(StatusCodes.BadRequest, new Response(Status.Rejected(), "bad username : token provided") with ActionResponseComponent {
                                val action = Shutdown()
                            }.toJSON.prettyPrint)
                        lazy val goodRequest : String => HttpResponse = generatedToken =>
                            HttpResponse(StatusCodes.OK,new Response(Status.Accepted(), "message successfully logged in") with LoginResponseComponent {
                                val token = generatedToken
                            }.toJSON.prettyPrint)
                        val pResponse = Promise[HttpResponse]
                        val retrieve = db.run(dal.get(user.user))
                        retrieve onComplete {
                            case Success(Some(user)) => {
                                val generatedToken = "hee" //dal.generate()
                                pResponse success goodRequest(generatedToken)
                            }
                            case _ => pResponse success badRequest
                        }
                        Await.result(pResponse.future, Duration.Inf)
                        pResponse.future.value match {
                            case Some(r) => r
                            case _ => badRequest
                        }
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


