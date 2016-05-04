name := "constructr-coordination-redis"

libraryDependencies ++= Vector(
  Library.constructrAkka,
  Library.rediscala,
  Library.akkaTestkit % "test",
  Library.scalaTest   % "test"
)
