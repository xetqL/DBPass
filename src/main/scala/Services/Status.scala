package DBPass.Services

/**
 * Created by xetql on 05.08.2015.
 */
trait Status

trait UserStatus extends Status

trait ActionStatus extends Status

object Status{

    case class Unauthorized () extends Status

    case class Accepted () extends Status

    case class Rejected () extends Status

    case class Connected () extends Status

    case class Disconnected () extends Status

    def apply(identifier: String) : Status = identifier match {
        case "Accepted" => new Accepted
        case "Rejected" => new Rejected
        case "Connected"=> new Connected
        case "Disconnected"=> new Disconnected
        case "Unauthorized"=> new Unauthorized
    }
}


