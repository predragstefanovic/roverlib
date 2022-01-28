package com.ps.rover

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

import scala.util.{Failure, Success, Try}

object Rover {

  sealed trait Message
  final case class Position(x: Int, y: Int, orientation: Orientation) extends Message
  final case class Error(desc: String) extends Message

  final case class CommandWrapper(command: Command, replyTo: ActorRef[Message])

  sealed trait Command
  final case class Initialize(position: Position) extends Command
  final case class MoveInstructions(str: String) extends Command

  protected sealed trait MoveCommand
  case object Forward extends MoveCommand
  case object Backward extends MoveCommand
  case object Left extends MoveCommand
  case object Right extends MoveCommand

  sealed trait Orientation(val left: Orientation, val right: Orientation)
  case object North extends Orientation(West, East)
  case object East extends Orientation(North, South)
  case object South extends Orientation(East, West)
  case object West extends Orientation(South, North)

  def apply(): Behavior[CommandWrapper] = uninitialized()

  private def uninitialized(): Behavior[CommandWrapper] = {
    Behaviors.receive {
      case (_, CommandWrapper(Initialize(position), replyTo)) =>
        replyTo ! position
        initialized(position)
      case (_, CommandWrapper(_, replyTo)) =>
        replyTo ! Error("Unsupported command!")
        Behaviors.same
    }
  }

  private def initialized(currP: Position): Behavior[CommandWrapper] = {
    Behaviors.receivePartial {
      case (_, CommandWrapper(MoveInstructions(instr), replyTo)) =>

        Try {
          MoveInstructions.moveCommands(instr)
            .foldLeft(currP) { (acc, mc) =>
              (mc match {
                case Forward => Position.stepForward
                case Backward => Position.stepBackward
                case Left => Position.rotateLeft
                case Right => Position.rotateRight
              })(acc)
            }
        } match {
          case Success(newP) =>
            replyTo ! newP
            initialized(newP)
          case Failure(e) =>
            replyTo ! Error(e.getMessage)
            Behaviors.same
        }
      case (_, CommandWrapper(_, replyTo)) =>
        replyTo ! Error("Unsupported command")
        Behaviors.same
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

  object MoveInstructions {
    def moveCommands(str: String): Seq[MoveCommand] =
      str.toSeq.map {
        case 'F' => Forward
        case 'B' => Backward
        case 'L' => Left
        case 'R' => Right
        case _ => throw new IllegalArgumentException
      }
  }
}


