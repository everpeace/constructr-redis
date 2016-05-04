lazy val construtrRoot = project
  .copy(id = "constructr-redis-root")
  .in(file("."))
  .enablePlugins(GitVersioning)
  .aggregate(constructrCoordinationRedis)

lazy val constructrCoordinationRedis = project
  .copy(id = "constructr-coordination-redis")
  .in(file("constructr-coordination-redis"))
  .enablePlugins(AutomateHeaderPlugin)
