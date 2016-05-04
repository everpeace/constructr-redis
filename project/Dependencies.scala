import sbt._

object Version {
    final val Scala            = "2.11.8"
    final val Constructr       = "0.13.2"
    final val Rediscala        = "1.6.0"
    final val Akka             = "2.4.4"
    final val AkkaLog4j        = "1.1.3"
    final val Log4j            = "2.5"
    final val ScalaTest        = "2.2.6"
}

object Library {
  val constructrCoordination = "de.heikoseeberger"        %% "constructr-coordination"  % Version.Constructr
  val constructrAkka         = "de.heikoseeberger"        %% "constructr-akka"          % Version.Constructr
  val rediscala              = "com.github.etaty"         %% "rediscala"                % Version.Rediscala
  val akkaCluster            = "com.typesafe.akka"        %% "akka-cluster"             % Version.Akka
  val akkaTestkit            = "com.typesafe.akka"        %% "akka-testkit"             % Version.Akka
  val akkaMultiNodeTestkit   = "com.typesafe.akka"        %% "akka-multi-node-testkit"  % Version.Akka
  val akkaLog4j              = "de.heikoseeberger"        %% "akka-log4j"               % Version.AkkaLog4j
  val log4jCore              = "org.apache.logging.log4j" %  "log4j-core"               % Version.Log4j
  val scalaTest              = "org.scalatest"            %% "scalatest"                % Version.ScalaTest
}
