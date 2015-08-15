organization := "ch.dbpass"

name := "DBPass"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.6"

scalacOptions := Seq("-deprecation", "-encoding", "utf8")

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

unmanagedBase <<= baseDirectory { base => base / "lib" }

libraryDependencies ++= {
    val akkaV = "2.3.9"
    val sprayV = "1.3.3"
    Seq(
        "io.spray" %% "spray-can" % sprayV,
        "io.spray" %% "spray-routing" % sprayV,
        "io.spray" %% "spray-testkit" % sprayV % "test",
        "io.spray" %% "spray-json" % "1.3.2",
        "com.typesafe.akka" %% "akka-actor" % akkaV,
        "com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
        "org.specs2" %% "specs2-core" % "2.3.11" % "test",
        "com.typesafe.slick" %% "slick" % "3.0.0",
        "org.slf4j" % "slf4j-nop" % "1.6.4",
        "com.h2database" % "h2" % "1.3.175",
        "org.scalatest" %% "scalatest" % "2.2.4" % "test"
    )
}

parallelExecution in Test := false

logBuffered := false

fork in run := true

connectInput in run := true