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

package com.github.everpeace.constructr.coordination
package redis

import _root_.redis.RedisClient
import akka.Done
import akka.actor.{ActorRefFactory, ActorSystem, Address, AddressFromURIString}
import com.typesafe.config.{Config, ConfigException}
import de.heikoseeberger.constructr.coordination.Coordination

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration

final class RedisCoordination(
    val clusterName: String,
    val system: ActorSystem
) extends Coordination {
  private[this] implicit val _system: ActorRefFactory = system
  private[this] implicit val _ec: ExecutionContext = _system.dispatcher

  private[this] val redis = RedisClientFactory.fromConfig(system.settings.config)

  def lockKey = s"$clusterName/lock"
  def nodeKeyPrefix = s"$clusterName/nodes"
  def nodeKey(self: Address) = s"$nodeKeyPrefix/${self.toString}"

  /**
   * Get the nodes.
   * @return future of nodes
   */
  override def getNodes(): Future[Set[Address]] = redis.keys(s"$nodeKeyPrefix/*").map(ks => scala.collection.immutable.Seq(ks: _*)).flatMap { ks =>
    // N+1 problem would happen here.
    // But N might not be so large in real situation.
    val f: Seq[Future[Option[Address]]] = for {
      k <- ks
    } yield {
      for {
        bytesOpt <- redis.get[String](k)
      } yield {
        bytesOpt.map(AddressFromURIString(_))
      }
    }
    Future.sequence(f).map { opts =>
      opts.filter(_.nonEmpty)
    }.map { somes =>
      somes.map(_.get)
    }.map {
      _.toSet
    }
  }

  /**
   * Acquire a lock for bootstrapping the cluster (first node).
   *
   * @param self self node
   * @param ttl  TTL for the lock
   * @return true, if lock could be akquired, else false
   */
  override def lock(self: Address, ttl: FiniteDuration): Future[Boolean] = {
    def readLock(): Future[Option[Address]] = redis.get[String](lockKey).map(_.map(AddressFromURIString(_)))
    def writeLock(lockHolder: Address): Future[Boolean] = {
      val trans2 = redis.watch(lockKey)
      val writeLock = trans2.set(lockKey, lockHolder.toString, None, Some(ttl.toMillis))
      trans2.exec()
      writeLock.recover {
        case _ => false
      }
    }
    readLock().flatMap {
      case Some(lockHolder) if lockHolder != self => Future.successful(false)
      case _                                      => writeLock(self)
    }
  }

  /**
   * Refresh entry for self.
   *
   * @param self self node
   * @param ttl  TTL for the node entry
   * @return future signaling done
   */
  override def refresh(self: Address, ttl: FiniteDuration): Future[Done] = addSelfOrRefresh(self, ttl)

  /**
   * Add self to the nodes.
   *
   * @param self self node
   * @param ttl  TTL for the node entry
   * @return future signaling done
   */
  override def addSelf(self: Address, ttl: FiniteDuration): Future[Done] = addSelfOrRefresh(self, ttl)

  // redis doesn't support element-wise ttl in [sorted] sets.
  // So, this sets with
  //   key -> {prefix}/{clusterName}/nodes/{encoded self}
  //   value -> encoded self
  private def addSelfOrRefresh(self: Address, ttl: FiniteDuration): Future[Done] = redis.set(nodeKey(self), self.toString, None, Some(ttl.toMillis)).map(_ => Done)

}

object RedisClientFactory {
  def fromConfig(config: Config)(implicit af: ActorRefFactory): RedisClient = {
    val host = Option(config.getString("constructr.coordination.host")).filter(_.trim.nonEmpty).getOrElse("")
    val port = config.getInt("constructr.coordination.port")
    require(host.nonEmpty, "\"constructr.coordination.redis.host\" must be given.")
    require(port > 0, "\"constructr.coordination.redis.port\" must be positive integer.")

    val password = try {
      Option(config.getString("constructr.coordination.redis.password")).filter(_.nonEmpty)
    } catch {
      case _: ConfigException.Missing => None
    }
    val db = try {
      Option(config.getInt("constructr.coordination.redis.db"))
    } catch {
      case _: ConfigException.Missing => None
    }

    RedisClient(host, port, password, db)
  }

}
