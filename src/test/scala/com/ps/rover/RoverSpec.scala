package com.ps.rover

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.ps.rover.Rover.{Backward, Command, CommandWrapper, East, Forward, Initialize, Left, North, Position, Right, South}
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.wordspec.AnyWordSpecLike

class RoverSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike with TableDrivenPropertyChecks {

  "A Rover" must {
    "initialize and reply its Position" in {
      val probe = createTestProbe[Position]()
      val rover = spawn(Rover())
      rover ! CommandWrapper(Initialize(Position(4, 2, East)), probe.ref)
      probe.expectMessage(Position(4, 2, East))
    }

    "allow only one initialization" in {
      val probe = createTestProbe[Position]()
      val rover = spawn(Rover())
      rover ! CommandWrapper(Initialize(Position(4, 2, East)), probe.ref)
      probe.expectMessage(Position(4, 2, East))
      rover ! CommandWrapper(Initialize(Position(4, 2, East)), probe.ref)
      probe.expectNoMessage()
    }

    "move in all directions" in {
      val probe = createTestProbe[Position]()
      val rover = spawn(Rover())
      rover ! CommandWrapper(Initialize(Position(4, 2, East)), probe.ref)
      probe.expectMessage(Position(4, 2, East))

      val cases = Table(
        ("cmd", "position"),
        (Forward, Position(5, 2, East)),
        (Backward, Position(4, 2, East)),
        (Left, Position(4, 2, North)),
        (Right, Position(4, 2, East)),
      )

      forAll(cases) { (cmd: Command, position: Position) =>
        rover ! CommandWrapper(cmd, probe.ref)
        probe.expectMessage(position)
      }
    }
  }

}
