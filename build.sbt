import com.typesafe.sbt.SbtStartScript

organization := "com.github.synesso"

name := "chess"

version := "0.1"

scalaVersion := "2.10.2"

seq(webSettings :_*)


libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "1.0.1",
  "com.novus" %% "salat-core" % "1.9.2" exclude("org.scala-lang", "scalap"),
  "org.postgresql" % "postgresql" % "9.2-1003-jdbc4",
  "org.scalatra" %% "scalatra" % "2.2.1",
  "org.scalatra" %% "scalatra-scalate" % "2.2.1",
  "org.scalatra" %% "scalatra-json" % "2.2.1",
  "org.eclipse.jetty" % "jetty-server" % "9.0.1.v20130408" % "compile;container",
  "org.eclipse.jetty" % "jetty-webapp" % "9.0.1.v20130408" % "compile;container",
  "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "compile;container" artifacts (Artifact("javax.servlet", "jar", "jar")),
  "org.json4s" %% "json4s-jackson" % "3.2.5",
  "org.slf4j" % "slf4j-simple" % "1.7.5",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
  "com.google.api-client" % "google-api-client" % "1.17.0-rc",
  "com.google.apis" % "google-api-services-plus" % "v1-rev99-1.17.0-rc",
  "com.google.apis" % "google-api-services-oauth2" % "v2-rev51-1.17.0-rc",
  "com.google.http-client" % "google-http-client-jackson2" % "1.14.1-beta",
  "com.github.synesso" %% "eshq" % "0.1",
  "me.lessis" %% "courier" % "0.1.2",
  "org.specs2" %% "specs2" % "2.2" % "test",
  "org.scalacheck" %% "scalacheck" % "1.10.0" % "test"
)

resolvers ++= Seq(
  "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/",
  "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
  "softprops-maven" at "http://dl.bintray.com/content/softprops/maven"
)

seq(SbtStartScript.startScriptForClassesSettings: _*)

ivyScala ~= { (is: Option[IvyScala]) =>
  for(i <- is) yield
    i.copy(checkExplicit = false)
}

scalacOptions in Test ++= Seq("-Yrangepos", "-language:reflectiveCalls")

net.virtualvoid.sbt.graph.Plugin.graphSettings

filterScalaLibrary := false
