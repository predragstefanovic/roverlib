package com.ps.rover

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

import scala.util.{Failure, Success, Try}

object Rover {

  final case class RoverCommand(moveInstructions: MoveInstructions, replyTo: ActorRef[RoverReply])

  sealed trait RoverReply
  final case class Position(x: Int, y: Int, orientation: Orientation) extends RoverReply
  final case class Error(desc: String) extends RoverReply

  sealed trait Orientation(val left: Orientation, val right: Orientation)
  case object North extends Orientation(West, East)
  case object East extends Orientation(North, South)
  case object South extends Orientation(East, West)
  case object West extends Orientation(South, North)

  final case class MoveInstructions(str: String)
  protected sealed trait MoveCommand
  protected case object Forward extends MoveCommand
  protected case object Backward extends MoveCommand
  protected case object Left extends MoveCommand
  protected case object Right extends MoveCommand

  def apply(position: Position, replyTo: ActorRef[RoverReply]): Behavior[RoverCommand] = {
    replyTo ! position
    initialized(position)
  }

  def apply(position: Position): Behavior[RoverCommand] = {
    initialized(position)
  }

  private def initialized(currP: Position): Behavior[RoverCommand] = {
    Behaviors.receive {
      case (context, RoverCommand(MoveInstructions(instr), replyTo)) =>
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


