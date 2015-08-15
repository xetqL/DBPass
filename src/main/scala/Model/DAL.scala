package Model

import slick.driver.JdbcProfile
/**
 * Created by xetqL on 12.08.2015.
 */
class DAL(val driver: JdbcProfile) extends DriverComponent with UsersComponent with AuthTokensComponent with KeysComponent{
    import driver.api._

    def drop   : DBIO[Unit] = (users.schema ++ keys.schema ++ authTokens.schema).drop

    def create : DBIO[Unit] = (users.schema ++ keys.schema ++ authTokens.schema).create

}

