package util
import java.sql.DriverManager
import Model.{DAL, User}
import Model.User._
import spray.routing.authentication.UserPass
import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.concurrent.Future
import slick.jdbc.JdbcBackend.Database
import scala.concurrent.ExecutionContext.Implicits.global
/**
 * Created by xetqL on 12.08.2015.
 */
object Util {
    /** A helper function to unload all JDBC drivers so we don't leak memory */
    def unloadDrivers {
        DriverManager.getDrivers.asScala.foreach { d =>
            DriverManager.deregisterDriver(d)
        }
    }

    implicit class ListUtil[A] (data: List[A]){
        def containsAll(list: List[A]): Boolean ={
            @tailrec
            def containsAllRec(list:List[A], acc:List[A]) : Boolean = {
                acc match {
                    case Nil => true
                    case head :: tail => if(list.contains(head)) containsAllRec(list, tail) else containsAllRec(list, acc)
                }
            }
            containsAllRec(data.distinct, list.distinct)
        }
    }

    def cypher(word:String) = word

    def userPassAuth(userPass: Option[UserPass])(implicit db:Database, dal:DAL): Future[Option[User]] = {
        userPass match {
            case Some(u) => db.run(dal.get(u.user))
            case _ => Future {
                None
            }
        }
    }
}
