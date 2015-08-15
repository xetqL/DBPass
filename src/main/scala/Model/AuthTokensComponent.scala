package Model

import java.sql.Timestamp
import java.util.Calendar

import slick.lifted.ProvenShape

/**
 * Created by xetqL on 12.08.2015.
 */
case class Token(tokenID: Int, token: String, expireDate: Timestamp, active: Boolean, userID: Int) {
    def this(token: String, expireDate: Timestamp, active: Boolean, userID: Int) = this(0, token, expireDate, active, userID)
}

object Token extends ((Int, String, Timestamp, Boolean, Int) => Token) {
    def apply(token: String, expireDate: Timestamp, active: Boolean, userID: Int) = new Token(0, token, expireDate, active, userID)
}

trait AuthTokensComponent {
    this: DriverComponent with UsersComponent =>

    import driver.api._

    class AuthTokens(tag: Tag) extends Table[Token](tag, "AuthTokens") {
        def authTokenID: Rep[Int] = column[Int]("tokenid", O.PrimaryKey, O.AutoInc)

        def authToken: Rep[String] = column[String]("token")

        def expireDate: Rep[Timestamp] = column[Timestamp]("expireDate")

        def active: Rep[Boolean] = column[Boolean]("active", O.Default(true))

        def userID: Rep[Int] = column[Int]("userid")

        def user = foreignKey("userfk_token", userID, users)(_.userID, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)

        def * = (authTokenID, authToken, expireDate, active, userID) <>(Token.tupled, Token.unapply)
    }

    val authTokens = TableQuery[AuthTokens]

    def generate(forWho: Int, expires_in: Int = 3600)(implicit db: slick.jdbc.JdbcBackend.Database): (String) = {
        val token = java.util.UUID.randomUUID().toString
        db.run(authTokens += Token(token, new Timestamp(Calendar.getInstance().getTime().getTime() + expires_in), true, forWho))
        token
    }

    def get(forWho: Int, token: String) = {
        val nowTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime())
        val q = authTokens
            .filter(_.userID === forWho)
            .filter(_.authToken === token)
            .filter(_.active === true)
            .filter(_.expireDate > nowTimestamp)
        q.result.headOption
    }
}
