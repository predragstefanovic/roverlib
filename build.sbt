organization := "com.ps"
name := "roverlib"

val akkaVersion = "2.6.18"

scalaVersion := "3.1.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
)