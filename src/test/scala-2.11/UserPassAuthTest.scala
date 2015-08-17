import Model.{User, DAL}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{BeforeAndAfter, FunSuite}
import slick.driver.H2Driver
import slick.jdbc.JdbcBackend.Database

import util.Util.userPassAuth
/**
 * Created by xetqL on 15.08.2015.
 */
class UserPassAuthTest extends FunSuite with BeforeAndAfter with ScalaFutures {
    implicit override val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))
    val dal = new DAL(H2Driver)
    var db: Database = _

    def createSchema() =
        db.run(dal.create).futureValue

    def insertUser(u:User): Int =
        db.run(dal.insert(u)).futureValue

    before {
        db = Database.forConfig("h2mem1")
    }

    val goodUser = User("admin", "pass")

    test("UserPassAuth works correctly"){
        createSchema()
        insertUser(goodUser)
        val r = userPassAuth(Some(goodUser))(db, dal).futureValue
        assert(r.exists(u => (u.username == goodUser.username) && (u.password == goodUser.password)))
    }

    after{
        db.close()
    }
}
