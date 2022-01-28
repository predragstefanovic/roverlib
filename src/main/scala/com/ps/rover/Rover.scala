package com.ps.rover

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object Rover {

  sealed trait Message
  final case class CommandWrapper(command: Command, replyTo: ActorRef[Position]) extends Message
  final case class Position(x: Int, y: Int, orientation: Orientation) extends Message

  sealed trait Command
  sealed trait MoveCommand extends Command
  case object Forward extends MoveCommand
  case object Backward extends MoveCommand
  case object Left extends MoveCommand
  case object Right extends MoveCommand
  final case class Initialize(position: Position) extends Command

  sealed trait Orientation(val left: Orientation, val right: Orientation)
  case object North extends Orientation(West, East)
  case object East extends Orientation(North, South)
  case object South extends Orientation(East, West)
  case object West extends Orientation(South, North)

  def apply(): Behavior[Message] = uninitialized()

  private def uninitialized(): Behavior[Message] = {
    Behaviors.receivePartial {
      case (_, CommandWrapper(Initialize(position), replyTo)) =>
        replyTo ! position
        initialized(position)
    }
  }

  private def initialized(currP: Position): Behavior[Message] = {
    Behaviors.receivePartial {
      case (_, CommandWrapper(cmd: MoveCommand, replyTo)) =>
        val newP = (cmd match {
          case Forward => Position.stepForward
          case Backward => Position.stepBackward
          case Left => Position.rotateLeft
          case Right => Position.rotateRight
        })(currP)

        replyTo ! newP
        initialized(newP)
    }
  }

  object Position {

    def rotateLeft(p: Position): Position = p.copy(orientation = p.orientation.left)
    def rotateRight(p: Position): Position = p.copy(orientation = p.orientation.right)
    def stepForward(p: Position): Position = step(p, 1)
    def stepBackward(p: Position): Position = step(p, -1)

    private def step(p: Position, i: Int): Position = p.orientation match {
      case North => Position(p.x, p.y + i, p.orientation)
      case South => Position(p.x, p.y - i, p.orientation)
      case East => Position(p.x + i, p.y, p.orientation)
      case West => Position(p.x - i, p.y, p.orientation)
    }
  }
}


