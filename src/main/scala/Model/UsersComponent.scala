package Model

/**
 * Created by xetqL on 12.08.2015.
 */
import util.Util
import spray.routing.authentication.UserPass

// custom case class mapping
case class User(userID: Int, username: String, password: String) {
    def this(username: String, password: String) = this(0, username, password)
}

object User extends((Int,String,String) => User){
    def apply(username: String, password: String) = new User(username, password)
    implicit def userPass2User(userPass:UserPass) = new User(userPass.user, userPass.pass)
    implicit def user2UserPass(user:User) = UserPass(user = user.username, pass = user.password)
}

trait UsersComponent {
    this: DriverComponent =>

    import driver.api._

    class Users(tag: Tag) extends Table[User](tag, "Users") {

        def userID: Rep[Int] = column[Int]("userid", O.PrimaryKey, O.AutoInc)

        def username: Rep[String] = column[String]("username", O.Length(50))

        def passwordHash: Rep[String] = column[String]("password_hash")

        def * = (userID, username, passwordHash) <> (User.tupled, User.unapply)
    }

    val users = TableQuery[Users]

    def insert(user: User) = users += user
    def insertAndRun(user:User)(implicit db : slick.jdbc.JdbcBackend.Database) = db.run(users += user)
    def get (user:User) = {
        val q = for {u <- users if (u.username === user.username) && (u.passwordHash === Util.cypher(user.password))} yield u
        q.result.headOption
    }
}
