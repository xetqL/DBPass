import Services.{LoginResponseComponent, ActionResponseComponent, Response}
import org.scalatest._
import DBPass.Services._

/**
 * Created by xetqL on 14.08.2015.
 */
class ResponsesTest extends FlatSpec with Matchers {
    "A response" should "be packed correctly" in {
        val x = new Response(Status.Accepted(), "good") with ActionResponseComponent with LoginResponseComponent{
            val token = "thisismytokenyo"
            val action = Action.Add()
        }
        x.toJSON.prettyPrint should (include("\"token\": \"thisismytokenyo\"") and include("\"action\": \"Add\""))
    }
}
