package DBPass.Services
/**
 * Created by xetqL on 05.08.2015.
 */

sealed trait Action

object Action {
    case class Add () extends Action

    case class Remove () extends Action

    case class Get () extends Action

    case class Login () extends Action

    case class Logout () extends Action

    case class Shutdown () extends Action

    def apply(identifier: String) : Action = identifier match {
        case "Add" => new Add
        case "Remove" => new Remove
        case "Get" => new Get
        case "Login" => new Login
        case "Logout" => new Logout
        case "Shutdown" => new Shutdown
    }
}
