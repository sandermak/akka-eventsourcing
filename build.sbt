val akkaVersion = "2.3.4"

resolvers += "krasserm at bintray" at "http://dl.bintray.com/krasserm/maven"

val project = Project(
  id = "concerts-akka-persistence",
  base = file("."),
  settings = Project.defaultSettings ++ Seq(
    name := "concerts-akka-persistence",
    version := "1.0",
    scalaVersion := "2.11.2",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
      "com.github.michaelpisula" %% "akka-persistence-inmemory" % "0.2.1",
      "org.scalatest" %% "scalatest" % "2.1.6" % "test",
      "commons-io" % "commons-io" % "2.4" % "test")
  )
)