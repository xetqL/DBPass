import java.sql.Timestamp
import java.util.Calendar

import Model.{User, DAL}
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.concurrent._
import slick.driver.H2Driver
import slick.driver.H2Driver.api.{Database => _, _}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.meta.MTable
import util.Util
/**
 * Created by xetqL on 15.08.2015.
 */
class SlickTest extends FunSuite with BeforeAndAfter with ScalaFutures{
    implicit override val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))
    val dal = new DAL(H2Driver)
    implicit var db: Database = _

    val testGoodUser = User("Jon", "Password")
    val testBadUser  = User("John", "Smith")

    def createSchema() =
        db.run(dal.create).futureValue

    def insertUser(u: User) : Int =
        db.run(dal.insert(u)).futureValue

    def check(u: User) = {
        db.run(dal.check(u).result).futureValue
    }

    def addTokenFor(forWho: User, token: String, expiresIn: Int = 3600)  = {
        db.run(dal.addTokenFor(forWho.userID, token, expiresIn)).futureValue
    }

    def refreshUser(user: User) : User = {
        val rUser = db.run(dal.users.filter(u => u.username === user.username && u.passwordHash === user.password).result).futureValue
        rUser.head
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

    test("Inserting a user add a user in the database and return its id") {
        createSchema()
        val userid1 = insertUser(testGoodUser)
        assert(userid1 == 1)
        val userid2 = insertUser(testGoodUser)
        assert(userid2 == 2)
        val userid3 = insertUser(testGoodUser)
        assert(userid3 == 3)
    }

    test("insert users works") {
        createSchema()
        insertUser(testGoodUser)
        dal.insertAndRun(testGoodUser)
        val results = db.run(dal.users.result).futureValue
        assert(results.size == 2)
        assert(results.head.username == testGoodUser.username)
    }

    test("Check user works") {
        createSchema()
        insertUser(testGoodUser)
        val goodUserExists = check(testGoodUser)
        val badUserExists = check(testBadUser)
        assert(goodUserExists === true)
        assert(badUserExists === false)
    }

    test("Token addTokenFor should insert a token for a user"){
        createSchema()
        insertUser(testGoodUser)
        val token = Util.generateToken
        val insertedGoodUser = refreshUser(testGoodUser)
        addTokenFor(insertedGoodUser, token)
        val tokens = db.run(dal.authTokens.result).futureValue
        assert(tokens.size > 0)
        assert(tokens.head.token == token)
    }

    test("Token getter should retrieve the most recent token"){
        createSchema()
        insertUser(testGoodUser)
        val insertedUser = refreshUser(testGoodUser)
        val nowTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime())
        addTokenFor(insertedUser, Util.generateToken, expiresIn = -100)
        addTokenFor(insertedUser, Util.generateToken, expiresIn = 3600)
        val recentToken = db.run(dal.getCurrent(insertedUser.userID)).futureValue
        assert(recentToken.head.expireDate.after(nowTimestamp))
    }

    test("Token getter should retrieve only valid tokens"){
        createSchema()
        insertUser(testGoodUser)
        val insertedUser = refreshUser(testGoodUser)
        val token1 = Util.generateToken
        addTokenFor(insertedUser, token1, expiresIn = -100)
        val recentToken = db.run(dal.getCurrent(insertedUser.userID)).futureValue
        assert(recentToken.size == 0)
    }

    after {
        db.close
    }
}
