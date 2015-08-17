package DBPass.Services


import java.lang.Exception

import DBPass.Services.Action.Shutdown
import Model.DAL
import Services.{LoginResponseComponent, ActionResponseComponent, Response}
import akka.actor.{ActorContext, Actor}
import slick.jdbc.JdbcBackend.Database
import spray.routing.authentication.{BasicAuth}
import scala.concurrent.{Future, Await, Promise}
import scala.concurrent.duration._
import spray.routing._
import spray.util._
import spray.http._
import Model.{User, Token}
import util.Util.userPassAuth
import scala.util.Success
import util.Util

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
                            lazy val responseOn: (StatusCode, String) => HttpResponse = (statusCode, message) =>
                                HttpResponse(statusCode, new Response(Status.Rejected(), message) with ActionResponseComponent {
                                    val action = Shutdown()
                                }.toJSON.prettyPrint)
                            try {
                                val token = p("token")
                                db.run(dal.get(p("username"))).flatMap {
                                    case Some(user) => db.run(dal.get(user.userID, token))
                                    case _ => Future {
                                        None
                                    }
                                } onComplete {
                                    case Success(Some(_)) => {
                                        in(2.second) {
                                            db.close()
                                            actorSystem.shutdown()
                                        }
                                        pResponse success responseOn(StatusCodes.OK, "shutdown scheduled")
                                    }
                                    case _ => pResponse success responseOn(StatusCodes.BadRequest, "inexistent or expired token")
                                }
                                Await.result(pResponse.future, Duration.Inf)
                                pResponse.future.value match {
                                    case Some(r) => r
                                    case _ => responseOn(StatusCodes.BadRequest, "unauthorized")
                                }
                            }catch{
                                case _ : Throwable => responseOn(StatusCodes.BadRequest, "unauthorized")
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
                        lazy val responseOnBadRequest =
                            HttpResponse(StatusCodes.BadRequest, new Response(Status.Rejected(), "bad username : token provided") with ActionResponseComponent {
                                val action = Shutdown()
                            }.toJSON.prettyPrint)
                        lazy val responseOnGoodRequest: String => HttpResponse = generatedToken =>
                            HttpResponse(StatusCodes.OK, new Response(Status.Accepted(), "message successfully logged in") with LoginResponseComponent {
                                val token = generatedToken
                            }.toJSON.prettyPrint)
                        lazy val token = Util.generateToken
                        val pResponse = Promise[HttpResponse]
                        db.run(dal.get(user.user)) onComplete {
                            case Success(Some(user)) => db.run(dal.addTokenFor(user.userID, token)) onComplete {
                                case Success(id) => pResponse success responseOnGoodRequest(token)
                                case _ => pResponse success responseOnBadRequest
                            }
                            case _ => pResponse success responseOnBadRequest
                        }
                        Await.result(pResponse.future, Duration.Inf)
                        pResponse.future.value match {
                            case Some(response) => response
                            case _ => responseOnBadRequest
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


