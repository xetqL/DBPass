import Model.{User, DAL}
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.concurrent._
import slick.driver.H2Driver
import slick.driver.H2Driver.api.{Database => _, _}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.meta.MTable

/**
 * Created by xetqL on 15.08.2015.
 */
class SlickTest extends FunSuite with BeforeAndAfter with ScalaFutures{
    implicit override val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))
    val dal = new DAL(H2Driver)
    implicit var db: Database = _

    def createSchema() =
        db.run(dal.create).futureValue

    def insertUser(): Int =
        db.run(dal.insert(User("Jon", "Password"))).futureValue

    def getUser(u:User) = {
        db.run(dal.get(u)).futureValue
    }

    before {
        db = Database.forConfig("h2mem1")
    }

    test("Creating the Schema works") {
        createSchema()
        val tables = db.run(MTable.getTables).futureValue
        assert(tables.size == 3)
        assert(tables.count(_.name.name.equalsIgnoreCase("users")) == 1)
        assert(tables.count(_.name.name.equalsIgnoreCase("authtokens")) == 1)
        assert(tables.count(_.name.name.equalsIgnoreCase("keys")) == 1)
    }

    test("Inserting a user works") {
        createSchema()
        val insertCount = insertUser()
        assert(insertCount == 1)
    }

    test("Query users works") {
        createSchema()
        insertUser()
        dal.insertAndRun(User("Jon", "Password"))
        val results = db.run(dal.users.result).futureValue
        assert(results.size == 2)
        assert(results.head.username == "Jon")
    }

    test("Get user works") {
        createSchema()
        insertUser()
        val results1 = getUser(User("Jon", "Password"))
        assert(results1.isDefined)
        val results2 = getUser(User("Nope", "Caca"))
        assert(!results2.isDefined)
    }

    after {
        db.close
    }
}
