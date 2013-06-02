import sbt._
import Keys._
import play.Project._

object Build extends Build {

  val dependencies = Seq(
    "org.webjars" % "bootstrap" % "2.3.2",
    "com.google.guava" % "guava" % "14.0.1",
    "com.google.code.findbugs" % "jsr305" % "1.3.9", // Guava's provided dependency but seems scalac needs it
    "org.webjars" %% "webjars-play" % "2.1.0-2",
    "com.google.zxing" % "javase" % "2.2"
  )

  val main = play.Project("mkqrcode", "1.0-SNAPSHOT", dependencies)
}
