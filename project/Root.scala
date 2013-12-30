import sbt._
import Keys._

object Root extends Build {
  lazy val root =
    Project("root", file("."))
      .configs(IntegrationTest)
      .settings(Defaults.itSettings: _*)
}