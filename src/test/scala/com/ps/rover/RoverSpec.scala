package com.ps.rover

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.ps.rover.Rover.{East, Initialize, Position}
import org.scalatest.wordspec.AnyWordSpecLike

class RoverSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "A Rover" must {
    "initialize and reply its Position" in {
      val probe = createTestProbe[Position]()
      val rover = spawn(Rover())
      rover ! Initialize(Position(4, 2, East), probe.ref)
      probe.expectMessage(Position(4, 2, East))
    }

    "allow only one initialization" in {
      val probe = createTestProbe[Position]()
      val rover = spawn(Rover())
      rover ! Initialize(Position(4, 2, East), probe.ref)
      probe.expectMessage(Position(4, 2, East))
      rover ! Initialize(Position(4, 2, East), probe.ref)
      probe.expectNoMessage()
    }
  }

}
