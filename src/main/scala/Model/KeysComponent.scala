package Model

import slick.lifted.ProvenShape

/**
 * Created by xetqL on 12.08.2015.
 */

trait KeysComponent { this: DriverComponent with UsersComponent =>
    import driver.api._

    class Keys(tag: Tag) extends Table[(Int, String, Int)](tag, "Keys") {
        def pwdID: Rep[Int] = column[Int]("pwdid", O.PrimaryKey, O.AutoInc)

        def userID : Rep[Int] = column[Int]("userid")

        def user = foreignKey("userfk_keys", userID, users)(_.userID, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)

        def forWhat: Rep[String] = column[String]("token", O.Length(255))
        // scalastyle:off method.name
        def * : ProvenShape[(Int, String, Int)] = (pwdID , forWhat, userID)
        // scalastyle:on method.name
    }

    val keys = TableQuery[Keys]
}