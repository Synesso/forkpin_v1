import com.typesafe.startscript.StartScriptPlugin

organization := "com.github.synesso"

name := "chess"

version := "0.1"

scalaVersion := "2.10.1"

seq(webSettings :_*)

classpathTypes ~= (_ + "orbit")

libraryDependencies ++= Seq(
  "org.scalatra" % "scalatra_2.10" % "2.2.1",
  "org.scalatra" % "scalatra-scalate_2.10" % "2.2.1",
  "org.eclipse.jetty" % "jetty-webapp" % "9.0.1.v20130408" % "container",
  "org.eclipse.jetty" % "jetty-server" % "9.0.1.v20130408" % "container",
  "org.eclipse.jetty" % "jetty-webapp" % "9.0.1.v20130408",
  "org.eclipse.jetty" % "jetty-server" % "9.0.1.v20130408",
  "org.slf4j" % "slf4j-simple" % "1.7.5",
  "org.eclipse.jetty" % "jetty-webapp" % "8.1.7.v20120910" % "container,compile",
  "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container,compile" artifacts Artifact("javax.servlet", "jar", "jar")
)

resolvers += "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/"

seq(StartScriptPlugin.startScriptForClassesSettings: _*)