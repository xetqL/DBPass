package Model

/**
 * Created by xetqL on 12.08.2015.
 */
import util.Util
import spray.routing.authentication.UserPass
import slick.jdbc.JdbcBackend.Database
import scala.language.implicitConversions
import scala.language.postfixOps

// custom case class mapping
case class User(userID: Int, username: String, password: String) {
    def this(username: String, password: String) = this(0, username, password)
}

object User extends((Int, String, String) => User){
    def apply(username: String, password: String) = new User(username, password)
    implicit def userPass2User(userPass: UserPass) = new User(userPass.user, userPass.pass)
    implicit def user2UserPass(user: User) = UserPass(user = user.username, pass = user.password)
}

trait UsersComponent {
    this: DriverComponent =>

    import driver.api.{Database => _, _}

    class Users(tag: Tag) extends Table[User](tag, "Users") {

        def userID: Rep[Int] = column[Int]("userid", O.PrimaryKey, O.AutoInc)

        def username: Rep[String] = column[String]("username", O.Length(50))

        def passwordHash: Rep[String] = column[String]("password_hash")

        // scalastyle:off method.name
        def * = (userID, username, passwordHash) <> (User.tupled, User.unapply)
        // scalastyle:on method.name
    }

    val users = TableQuery[Users]

    def insert(user: User) = (users returning users.map(_.userID)) += user

    def insertAndRun(user: User)(implicit db : Database) = db.run(insert(user))

    def check(user: User) = users filter (u => (u.username === user.username) && (u.passwordHash === Util.cypher(user.password))) exists

    def get(username: String) = {
        val q = for {u <- users if (u.username === username)} yield u
        q.result.headOption
    }
}
