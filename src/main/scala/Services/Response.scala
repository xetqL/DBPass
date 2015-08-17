package Services

import DBPass.Services.JsonProtocol._
import DBPass.Services.{Action, Status}
import spray.json._
/**
 * Created by aboul on 11.08.2015.
 */

sealed trait TResponse {
    val status: Status
    val message: String
    def toJSON : JsObject = JsObject(
        "status" -> JsString(status.getClass.getSimpleName),
        "message"-> JsString(message)
    )
}

trait LoginResponseComponent extends TResponse { this : TResponse =>
    def token: String
    override def toJSON = JsObject((super.toJSON.fields + ("token" -> JsString(token))).toSeq: _*)
}

trait ActionResponseComponent extends TResponse { this : TResponse =>
    def action: Action
    override def toJSON = JsObject((super.toJSON.fields + ("action" -> JsString(action.getClass.getSimpleName))).toSeq: _*)
}

case class Response(val status: Status, val message: String) extends TResponse