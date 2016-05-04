name := "constructr-coordination-redis"

libraryDependencies ++= Vector(
  Library.constructrCoordination,
  Library.rediscala,
  Library.akkaTestkit % "test",
  Library.scalaTest   % "test"
)
