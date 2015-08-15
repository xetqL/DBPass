package DBPass.Services

import spray.json._

/**
 * Created by aboul on 07.08.2015.
 */
object JsonProtocol extends DefaultJsonProtocol {

    case class Person(LoginStatus: Boolean, Username: String)

    implicit val personFormat = jsonFormat2(Person)

    implicit object actionFormat extends RootJsonFormat[Action]{
        val actionFieldName = "Action"
        def write(a: Action): JsObject = JsObject(actionFieldName -> JsString(a.getClass.getSimpleName))

        def read(value: JsValue): Action = Action(value.asJsObject.getFields(actionFieldName).head.toString)
    }

    implicit object statusFormat extends RootJsonFormat[Status]{
        val statusFieldName = "Status"
        def write(a: Status): JsObject = JsObject(statusFieldName -> JsString(a.getClass.getSimpleName))

        def read(value: JsValue): Status = Status(value.asJsObject.getFields(statusFieldName).head.toString)
    }

}
