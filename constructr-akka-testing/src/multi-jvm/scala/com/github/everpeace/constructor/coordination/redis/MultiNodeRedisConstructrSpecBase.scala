/*
 * Copyright 2016 Shingo Omura
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.everpeace.constructor.coordination.redis

import akka.actor.ActorDSL.{Act, actor}
import akka.actor.Address
import akka.cluster.{Cluster, ClusterEvent}
import akka.pattern.ask
import akka.remote.testkit.{MultiNodeConfig, MultiNodeSpec}
import akka.stream.ActorMaterializer
import akka.testkit.TestDuration
import akka.util.Timeout
import com.github.everpeace.constructr.coordination.redis.RedisClientFactory
import com.typesafe.config.ConfigFactory
import de.heikoseeberger.constructr.akka.ConstructrExtension
import de.heikoseeberger.constructr.coordination.Coordination
import org.scalatest.{BeforeAndAfterAll, FreeSpecLike, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object ConstructrMultiNodeConfig {
  val coordinationHost = {
    val dockerHostPattern = """tcp://(\S+):\d{1,5}""".r
    sys.env.get("DOCKER_HOST")
      .collect { case dockerHostPattern(address) => address }
      .getOrElse("127.0.0.1")
  }
}

class ConstructrMultiNodeConfig(coordinationPort: Int) extends MultiNodeConfig {

  import ConstructrMultiNodeConfig._

  commonConfig(ConfigFactory.load())
  for (n <- 1.to(5)) {
    val port = 2550 + n
    nodeConfig(role(port.toString))(ConfigFactory.parseString(
      s"""|akka.actor.provider            = akka.cluster.ClusterActorRefProvider
          |akka.remote.netty.tcp.hostname = "127.0.0.1"
          |akka.remote.netty.tcp.port     = $port
          |constructr.coordination.host   = $coordinationHost
          |constructr.coordination.port   = $coordinationPort
          |""".stripMargin
    ))
  }
}

abstract class MultiNodeRedisConstructrSpecBase(coordinationPort: Int, prefix: String, clusterName: String)
  extends MultiNodeSpec(new ConstructrMultiNodeConfig(coordinationPort))
    with FreeSpecLike with Matchers with BeforeAndAfterAll {

  implicit val mat = ActorMaterializer()
  val redis = RedisClientFactory.fromConfig(system.settings.config)
  val redisCoordination = Coordination(prefix, clusterName, system)

  "Constructr should manage an Akka cluster" in {
    runOn(roles.head) {
      within(20.seconds.dilated) {
        awaitAssert {
          val coordinationStatus = Await.result(
            redis.flushdb(),
            5.seconds.dilated
          )
          coordinationStatus should be(true)
        }
      }
    }

    enterBarrier("coordination-started")

    ConstructrExtension(system)
    val listener = actor(new Act {

      import ClusterEvent._

      var isMember = false
      Cluster(context.system).subscribe(self, InitialStateAsEvents, classOf[MemberJoined], classOf[MemberUp])
      become {
        case "isMember" => sender() ! isMember
        case MemberJoined(member) if member.address == Cluster(context.system).selfAddress => isMember = true
        case MemberUp(member) if member.address == Cluster(context.system).selfAddress => isMember = true
      }
    })
    within(20.seconds.dilated) {
      awaitAssert {
        implicit val timeout = Timeout(1.second.dilated)
        val isMember = Await.result((listener ? "isMember").mapTo[Boolean], 1.second.dilated)
        isMember shouldBe true
      }
    }

    enterBarrier("cluster-formed")

    within(5.seconds.dilated) {
      awaitAssert {
        import de.heikoseeberger.constructr.akka._
        val constructrNodes = Await.result(
          redisCoordination.getNodes[Address](),
          1.second.dilated
        )
        roles.to[Set].map(_.name.toInt) shouldEqual constructrNodes.flatMap(_.port)
      }
    }

    enterBarrier("done")
  }

  override def initialParticipants = roles.size

  override protected def beforeAll() = {
    super.beforeAll()
    multiNodeSpecBeforeAll()
  }

  override protected def afterAll() = {
    multiNodeSpecAfterAll()
    super.afterAll()
  }
}
