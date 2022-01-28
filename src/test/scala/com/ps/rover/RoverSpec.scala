package com.ps.rover

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.ps.rover.Rover.*
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.wordspec.AnyWordSpecLike

class RoverSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike with TableDrivenPropertyChecks {

  "A Rover" must {
    "initialize and reply its Position" in {
      val probe = createTestProbe[RoverReply]()
      val rover = spawn(Rover(Position(4, 2, East), probe.ref))
      probe.expectMessage(Position(4, 2, East))
    }

    "move in all directions" in {
      val probe = createTestProbe[RoverReply]()

      val cases = Table(
        ("instr", "position"),
        (MoveInstructions("FBLR"), Position(4, 2, East)),
        (MoveInstructions("RRRR"), Position(4, 2, East)),
        (MoveInstructions("LLLL"), Position(4, 2, East)),
        (MoveInstructions(""), Position(4, 2, East)),
        (MoveInstructions("FLFFFRFLB"), Position(6, 4, North)),
        (MoveInstructions("BBBBBBBB"), Position(-4, 2, East)),
        (MoveInstructions("RFFFFL"), Position(4, -2, East)),
      )

      forAll(cases) { (moveInstrCmd: MoveInstructions, position: Position) =>
        val rover = spawn(Rover(Position(4, 2, East)))

        rover ! RoverCommand(moveInstrCmd, probe.ref)
        probe.expectMessage(position)
      }
    }

    "fail on unsupported move commands" in {
      val probe = createTestProbe[RoverReply]()
      val rover = spawn(Rover(Position(4, 2, East)))

      rover ! RoverCommand(MoveInstructions("FR%L"), probe.ref)
      probe.expectMessageType[Error]
    }
  }

}
