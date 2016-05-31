package util

/**
  * Created by nico on 28/05/16.
  */

import akka.actor.Actor.Receive
import akka.actor.{ActorRef, Actor}
import controllers.Messages.InEvent
import play.api._

import scala.collection.immutable.Queue


object Global extends GlobalSettings {

  private var pendingPlayers = Queue[ActorRef]()

  def addPlayer(out: ActorRef) = {
    pendingPlayers = pendingPlayers enqueue out
  }
  def getFirstPendingPlayer: Option[ActorRef] = {
    if (pendingPlayers.isEmpty) None else {
      pendingPlayers.dequeue match {
        case  (result, queue) =>
          pendingPlayers = queue
          Some(result)
      }
    }
  }
}

//class GameActor(out: ActorRef) extends Actor {
//  override def receive: Receive = {
//    case e: InEvent => e toMessage match {
//
//    }
//  }
//}
