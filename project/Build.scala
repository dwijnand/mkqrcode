import sbt._
import Keys._
import play.Project._

object Build extends Build {

  val dependencies = Seq(
    "com.google.guava" % "guava" % "14.0.1",
    "com.google.code.findbugs" % "jsr305" % "1.3.9", // Guava's provided dependency but seems scalac needs it
    "com.google.zxing" % "javase" % "2.2"
  )

  val main = play.Project("mkqrcode", "1.0-SNAPSHOT", dependencies)
}
