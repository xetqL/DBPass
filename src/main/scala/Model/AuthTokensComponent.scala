package Model

import java.sql.Timestamp
import java.util.Calendar
import util.Util

/**
 * Created by xetqL on 12.08.2015.
 */
case class Token(tokenID: Int, token: String, expireDate: Timestamp, userID: Int) {
    def this(token: String, expireDate: Timestamp, userID: Int) = this(0, token, expireDate, userID)
}

object Token extends ((Int, String, Timestamp, Int) => Token) {
    def apply(token: String, expireDate: Timestamp, userID: Int) = new Token(0, token, expireDate, userID)
}

trait AuthTokensComponent {
    this: DriverComponent with UsersComponent =>

    import driver.api._

    class AuthTokens(tag: Tag) extends Table[Token](tag, "AuthTokens") {
        def authTokenID: Rep[Int] = column[Int]("tokenid", O.PrimaryKey, O.AutoInc)

        def authToken: Rep[String] = column[String]("token")

        def expireDate: Rep[Timestamp] = column[Timestamp]("expireDate")

        def userID: Rep[Int] = column[Int]("userid")

        def user = foreignKey("userfk_token", userID, users)(_.userID, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)

        // scalastyle:off method.name
        def * = (authTokenID, authToken, expireDate, userID) <>(Token.tupled, Token.unapply)
        // scalastyle:on method.name
    }

    val authTokens = TableQuery[AuthTokens]

    def addTokenFor(forWho:Int, token:String, expiresIn: Int = 3600)(implicit db: slick.jdbc.JdbcBackend.Database) = {
        authTokens returning authTokens.map(_.authTokenID) += Token(token, new Timestamp(Calendar.getInstance().getTime().getTime() + expiresIn), forWho)
    }

    /**
     *
     * @param forWho user id
     * @param token
     * @return
     */
    def get(forWho: Int, token: String) = {
        val nowTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime())
        val q = authTokens
            .filter(_.userID === forWho)
            .filter(_.authToken === token)
            .filter(_.expireDate > nowTimestamp)
            .sortBy(_.expireDate.asc)
            .take(1)
        q.result.headOption
    }
    def getCurrent(forWho: Int) = {
        val nowTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime())
        val q = authTokens
            .filter(_.userID === forWho)
            .filter(_.expireDate > nowTimestamp)
            .sortBy(_.expireDate.asc)
            .take(1)
        q.result.headOption
    }

}
