lazy val construtrRoot = project
  .copy(id = "constructr-redis-root")
  .in(file("."))
  .enablePlugins(GitVersioning)
  .aggregate(constructrCoordinationRedis, constructrAkkaTesting)

lazy val constructrCoordinationRedis = project
  .copy(id = "constructr-coordination-redis")
  .in(file("constructr-coordination-redis"))
  .enablePlugins(AutomateHeaderPlugin, BintrayPlugin)

lazy val constructrAkkaTesting = project
  .copy(id = "constructr-akka-testing")
  .in(file("constructr-akka-testing"))
  .enablePlugins(AutomateHeaderPlugin)
  .configs(MultiJvm)
  .dependsOn(constructrCoordinationRedis % "test->compile")
