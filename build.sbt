import com.typesafe.startscript.StartScriptPlugin

organization := "com.github.synesso"

name := "chess"

version := "0.1"

scalaVersion := "2.10.2"

seq(webSettings :_*)

classpathTypes ~= (_ + "orbit")

libraryDependencies ++= Seq(
  "com.typesafe.slick" % "slick_2.10" % "1.0.1",
  "org.postgresql" % "postgresql" % "9.2-1003-jdbc4",
  "org.scalatra" % "scalatra_2.10" % "2.2.1",
  "org.scalatra" % "scalatra-scalate_2.10" % "2.2.1",
  "org.scalatra" % "scalatra-json_2.10" % "2.2.1",
  "org.eclipse.jetty" % "jetty-server" % "9.0.1.v20130408",
  "org.eclipse.jetty" % "jetty-server" % "9.0.1.v20130408" % "container",
  "org.eclipse.jetty" % "jetty-webapp" % "9.0.1.v20130408",
  "org.eclipse.jetty" % "jetty-webapp" % "9.0.1.v20130408" % "container",
  "org.eclipse.jetty" % "jetty-webapp" % "8.1.7.v20120910" % "container,compile",
  "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container,compile" artifacts Artifact("javax.servlet", "jar", "jar"),
  "org.json4s" % "json4s-jackson_2.10" % "3.2.5",
  "org.slf4j" % "slf4j-simple" % "1.7.5",
  // "ch.qos.logback" % "logback-classic" % "1.0.11" % "runtime",
  "com.google.api-client" % "google-api-client" % "1.14.1-beta",
  "com.google.apis" % "google-api-services-plus" % "v1-rev62-1.14.1-beta",
  "com.google.apis" % "google-api-services-oauth2" % "v1-rev33-1.14.1-beta",
  "com.google.http-client" % "google-http-client-jackson2" % "1.14.1-beta"
)

resolvers ++= Seq(
  "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/",
  "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
)

seq(StartScriptPlugin.startScriptForClassesSettings: _*)

ivyScala ~= { (is: Option[IvyScala]) =>
  for(i <- is) yield
    i.copy(checkExplicit = false)
}
