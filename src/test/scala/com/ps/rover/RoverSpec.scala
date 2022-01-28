package com.ps.rover

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.ps.rover.Rover.*
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.wordspec.AnyWordSpecLike

class RoverSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike with TableDrivenPropertyChecks {

  "A Rover" must {
    "initialize and reply its Position" in {
      val probe = createTestProbe[Message]()
      val rover = spawn(Rover())
      rover ! CommandWrapper(Initialize(Position(4, 2, East)), probe.ref)
      probe.expectMessage(Position(4, 2, East))
    }

    "allow only one initialization" in {
      val probe = createTestProbe[Message]()
      val rover = spawn(Rover())
      rover ! CommandWrapper(Initialize(Position(4, 2, East)), probe.ref)
      probe.expectMessage(Position(4, 2, East))
      rover ! CommandWrapper(Initialize(Position(4, 2, East)), probe.ref)
      probe.expectMessageType[Error]
    }

    "move in all directions" in {
      val probe = createTestProbe[Message]()
      val initCmd = Initialize(Position(4, 2, East))

      val cases = Table(
        ("instr", "position"),
        (MoveInstructions("FBLR"), Position(4, 2, East)),
        (MoveInstructions("RRRR"), Position(4, 2, East)),
        (MoveInstructions("LLLL"), Position(4, 2, East)),
        (MoveInstructions(""), Position(4, 2, East)),
        (MoveInstructions("FLFFFRFLB"), Position(6, 4, North)),
      )

      forAll(cases) { (moveInstrCmd: MoveInstructions, position: Position) =>
        val rover = spawn(Rover())
        rover ! CommandWrapper(initCmd, probe.ref)
        probe.expectMessage(Position(4, 2, East))

        rover ! CommandWrapper(moveInstrCmd, probe.ref)
        probe.expectMessage(position)
      }
    }

    "fail on unsupported commands" in {
      val probe = createTestProbe[Message]()
      val rover = spawn(Rover())

      // before init
      rover ! CommandWrapper(MoveInstructions("FR%L"), probe.ref)
      probe.expectMessageType[Error]

      rover ! CommandWrapper(Initialize(Position(4, 2, East)), probe.ref)
      probe.expectMessage(Position(4, 2, East))

      // after init
      rover ! CommandWrapper(MoveInstructions("FR%L"), probe.ref)
      probe.expectMessageType[Error]

    }
  }

}
