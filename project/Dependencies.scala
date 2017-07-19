import sbt._

object Version {
    final val Scala            = Seq("2.11.8", "2.12.1")
    final val Constructr       = "0.17.0"
    final val Rediscala        = "1.8.0"
    final val Akka             = "2.5.0"
    final val AkkaLog4j        = "1.4.0"
    final val Log4j            = "2.5"
    final val ScalaTest        = "3.0.0"
}

object Library {
  val constructrCoordination = "de.heikoseeberger"        %% "constructr-coordination"  % Version.Constructr
  val constructrAkka         = "de.heikoseeberger"        %% "constructr"               % Version.Constructr
  val rediscala              = "com.github.etaty"         %% "rediscala"                % Version.Rediscala
  val akkaCluster            = "com.typesafe.akka"        %% "akka-cluster"             % Version.Akka
  val akkaTestkit            = "com.typesafe.akka"        %% "akka-testkit"             % Version.Akka
  val akkaMultiNodeTestkit   = "com.typesafe.akka"        %% "akka-multi-node-testkit"  % Version.Akka
  val akkaLog4j              = "de.heikoseeberger"        %% "akka-log4j"               % Version.AkkaLog4j
  val log4jCore              = "org.apache.logging.log4j" %  "log4j-core"               % Version.Log4j
  val scalaTest              = "org.scalatest"            %% "scalatest"                % Version.ScalaTest
}
