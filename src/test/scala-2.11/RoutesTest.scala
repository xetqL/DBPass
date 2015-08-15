import DBPass.Services.IDBService
import Model.{User, DAL}
import org.scalatest.{FunSuite, BeforeAndAfter}
import org.scalatest.concurrent.ScalaFutures
import slick.driver.H2Driver
import slick.jdbc.JdbcBackend.Database
import org.specs2.mutable.Specification
import spray.http.{HttpHeaders, HttpChallenge, StatusCodes, BasicHttpCredentials}
import spray.routing.AuthenticationFailedRejection
import spray.routing.AuthenticationFailedRejection.CredentialsRejected
import spray.testkit.Specs2RouteTest

/**
 * Created by xetqL on 14.08.2015.
 */
class RoutesTest extends Specification with Specs2RouteTest with IDBService with ScalaFutures {
    override val db = Database.forConfig("test")
    override val dal = new DAL(H2Driver)
    def actorRefFactory = system
    val goodUser = User("Jon", "Password")
    val unvalidCredentialsDirective = addCredentials(BasicHttpCredentials("administrateur", "password"))
    val validCredentialsDirective =   addCredentials(BasicHttpCredentials(goodUser.username, goodUser.password))
    val challenge = HttpHeaders.`WWW-Authenticate`(HttpChallenge("Basic", "secure site"))
    def createSchema() =
        db.run(dal.create).futureValue

    def insertUser(u:User) : Int =
        db.run(dal.insert(u)).futureValue


    createSchema()
    insertUser(goodUser)

    "The shutdown route" should {

        "return a rejected response when submitting a bad pair(username:token)" in {
            Get("/shutdown") ~> shutdownRoute ~> check {
                status === StatusCodes.BadRequest
            }
        }

        "return a accepted response when submitting a valid pair(username:token)" in {
            Get("/shutdown") ~> shutdownRoute ~> check {
                status === StatusCodes.OK
            }
        }
    }

    "The signup route" should {

        "return a rejected response when submitting bad credentials" in {
            Post("/signup") ~> unvalidCredentialsDirective ~> signupRoute ~> check {
                rejection === AuthenticationFailedRejection(CredentialsRejected, List(challenge))
            }
        }

        "return a accepted response when submitting good credentials" in {
            Post("/signup") ~> validCredentialsDirective ~> signupRoute ~> check {
                status === StatusCodes.OK
                responseAs[String] must contain("token")
            }
        }
    }
}