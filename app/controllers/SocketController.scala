package controllers

import javax.inject.Inject

import akka.actor._
import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.libs.json.Json
import play.api.mvc.WebSocket.MessageFlowTransformer
import play.api.mvc._
import play.api.libs.streams._
import util.Global

class SocketController @Inject()(implicit system: ActorSystem, materializer: Materializer) extends Controller{

  import Messages._

  implicit val inEventFormat = Json.format[InEvent]
  implicit val outEventFormat = Json.format[OutEvent]

  implicit val messageFlowTransformer = MessageFlowTransformer.jsonMessageFlowTransformer[InEvent, OutEvent]

  class MyWebSocketActor(out: ActorRef) extends Actor {

    def receive = {
      case e: InEvent => e toMessage match {
        case SearchGame =>
          Global getFirstPendingPlayer match {
            case  Some(v) =>
              v ! InEvent("match-player")
              out ! OutEvent("Matched Player")
            case None =>
              Global addPlayer self
              out ! OutEvent("Searching game...")
          }
        case MatchPlayer =>
          out ! OutEvent("Matched Player")
      }
    }
  }

  object MyWebSocketActor {
    def props(out: ActorRef) = Props(new MyWebSocketActor(out))
  }

  def socket = WebSocket.accept[InEvent, OutEvent] { request =>
    ActorFlow.actorRef(out => MyWebSocketActor.props(out))
  }

  def socketUrl = Action { request =>
    val url = "ws://" + request.host + routes.SocketController.socket.url
    Ok(url)
  }

}

object Messages {

  case class InEvent(action: String) {
    def toMessage: Message = {
      action match {
        case "search-game" => SearchGame
        case "match-player" => MatchPlayer
      }
    }
  }
  case class OutEvent(msg: String)

  trait Message

  case object SearchGame extends Message

  case object MatchPlayer extends Message

}