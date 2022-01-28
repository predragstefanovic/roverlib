package com.ps.rover

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object Rover {

    sealed trait Orientation
    case object North extends Orientation
    case object East extends Orientation
    case object South extends Orientation
    case object West extends Orientation

    final case class Position(x: Int, y: Int, orientation: Orientation)

    sealed trait Command
    final case class Initialize(position: Position, replyTo: ActorRef[Position]) extends Command

    def apply(): Behavior[Command] = uninitialized()

    private def uninitialized(): Behavior[Command] = {
        Behaviors.receivePartial {
            case (context, Initialize(position, replyTo)) =>
                replyTo ! position
                initialized(position)
        }
    }

    private def initialized(currentPosition: Position): Behavior[Command] = Behaviors.stopped

}
