name := "Fractal Paint"

version := "1.0"

scalaVersion := "2.13.1"

libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "2.1.1"

Global / serverConnectionType := ConnectionType.Tcp

scalacOptions ++= Seq(
  "-feature",
  "-unchecked",
  "-language:higherKinds",
  "-language:postfixOps",
  "-deprecation"
)
