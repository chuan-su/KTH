/*
 * The MIT License
 *
 * Copyright 2017 Lars Kroll <lkroll@kth.se>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package se.kth.id2203.simulation

import org.scalatest._
import se.kth.id2203.{ParentComponent, kvstore}
import se.kth.id2203.networking._
import se.sics.kompics.network.Address
import java.net.{InetAddress, UnknownHostException}
import java.util.UUID

import se.kth.id2203.kvstore.OperationResponse
import se.sics.kompics.sl._
import se.sics.kompics.sl.simulator._
import se.sics.kompics.simulator.{SimulationScenario => JSimulationScenario}
import se.sics.kompics.simulator.run.LauncherComp
import se.sics.kompics.simulator.result.SimulationResultSingleton
import se.sics.kompics.simulator.network.impl.NetworkModels

import scala.concurrent.duration._
import collection.mutable

class OpsTest extends FlatSpec with Matchers {

  private val nMessages = 10
    behavior of "Infrastructure"

    it should "not fail" in {
      val seed = 1234l;
      JSimulationScenario.setSeed(seed);
      val simpleBootScenario = SimpleScenario.scenario(10)
      simpleBootScenario.simulate(classOf[LauncherComp])
    }
    it should "connect all abstraction levels" in {
      val seed = 123l
      JSimulationScenario.setSeed(seed)
      val simpleBootScenario = SimpleScenario.scenario(10)
      val res = SimulationResultSingleton.getInstance()
      SimulationResult += ("messages" -> nMessages)
      simpleBootScenario.simulate(classOf[LauncherComp])
      for (i <- 0 to nMessages) {
        SimulationResult.get[String](s"test$i") should be (Some("Ok"))
      }
    }

  behavior of "KV-store"

    it should "implement basic operations" in {
      val seed = 123l
      JSimulationScenario.setSeed(seed)
      val simpleBootScenario = SimpleScenario.scenario(10)
      val res = SimulationResultSingleton.getInstance()
      SimulationResult += ("messages" -> nMessages)
      simpleBootScenario.simulate(classOf[LauncherComp])
      for (i <- 0 to nMessages) {
        res.get(s"put$i", classOf[String]) should be ("PutSuccess")
        res.get(s"get$i", classOf[String]) should be (i.toString)
        res.get(s"cas$i", classOf[String]) should be ("Ok")
      }
    }


//  it should "fulfill linearizeability properties" in {
//    val seed = 123l
//    JSimulationScenario.setSeed(seed)
//    val simpleBootScenario = LinearizeableScenario.scenario(10)
//    val res = SimulationResultSingleton.getInstance()
//    SimulationResult += ("messages" -> nMessages)
//    simpleBootScenario.simulate(classOf[LauncherComp])
//    var queue = mutable.Map.empty[String, OperationItem]
//
//    //id: UUID, key: String, value: Option[Any], old_val: Option[Any], operator: String,method: String, statusCode: OpCode)
//    res.get(s"linear", classOf[String]) should be ("true")
//  }

}

object SimpleScenario {

  import Distributions._
  // needed for the distributions, but needs to be initialised after setting the seed
  implicit val random = JSimulationScenario.getRandom()

  private def intToServerAddress(i: Int): Address = {
    try {
      NetAddress(InetAddress.getByName("192.193.0." + i), 45678)
    } catch {
      case ex: UnknownHostException => throw new RuntimeException(ex)
    }
  }
  private def intToClientAddress(i: Int): Address = {
    try {
      NetAddress(InetAddress.getByName("192.193.1." + i), 45678)
    } catch {
      case ex: UnknownHostException => throw new RuntimeException(ex)
    }
  }

  private def isBootstrap(self: Int): Boolean = self == 1
  //val setUniformLatencyNetwork = () => Op.apply((_: Unit) => ChangeNetwork(NetworkModels.withUniformRandomDelay(3,7)))
  
  val startServerOp = Op { (self: Integer) =>

    val selfAddr = intToServerAddress(self)
    val conf = if (isBootstrap(self)) {
      // don't put this at the bootstrap server, or it will act as a bootstrap client
      Map("id2203.project.address" -> selfAddr)
    } else {
      Map(
        "id2203.project.address" -> selfAddr,
        "id2203.project.bootstrap-address" -> intToServerAddress(1))
    }
    StartNode(selfAddr, Init.none[ParentComponent], conf);
  }

  val startClientOp = Op { (self: Integer) =>
    val selfAddr = intToClientAddress(self)
    val conf = Map(
      "id2203.project.address" -> selfAddr,
      "id2203.project.bootstrap-address" -> intToServerAddress(1))
    StartNode(selfAddr, Init.none[ScenarioClient], conf);
  }

  def scenario(servers: Int): JSimulationScenario = {

    //val networkSetup = raise(1, setUniformLatencyNetwork()).arrival(constant(0))
    val startCluster = raise(servers, startServerOp, 1.toN).arrival(constant(1.second))
    val startClients = raise(1, startClientOp, 1.toN).arrival(constant(1.second))

    //startCluster andThen
//    networkSetup andThen
//      0.seconds afterTermination startCluster andThen
//      10.seconds afterTermination startClients andThen
//      100.seconds afterTermination Terminate
    startCluster andThen
      10.seconds afterTermination startClients andThen
      100.seconds afterTermination Terminate
  }
}

object LinearizeableScenario {

  import Distributions._
  // needed for the distributions, but needs to be initialised after setting the seed
  implicit val random = JSimulationScenario.getRandom()

  private def intToServerAddress(i: Int): Address = {
    try {
      NetAddress(InetAddress.getByName("192.193.0." + i), 45678)
    } catch {
      case ex: UnknownHostException => throw new RuntimeException(ex)
    }
  }
  private def intToClientAddress(i: Int): Address = {
    try {
      NetAddress(InetAddress.getByName("192.193.1." + i), 45678)
    } catch {
      case ex: UnknownHostException => throw new RuntimeException(ex)
    }
  }

  private def isBootstrap(self: Int): Boolean = self == 1
  //val setUniformLatencyNetwork = () => Op.apply((_: Unit) => ChangeNetwork(NetworkModels.withUniformRandomDelay(3,7)))

  val startServerOp = Op { (self: Integer) =>

    val selfAddr = intToServerAddress(self)
    val conf = if (isBootstrap(self)) {
      // don't put this at the bootstrap server, or it will act as a bootstrap client
      Map("id2203.project.address" -> selfAddr)
    } else {
      Map(
        "id2203.project.address" -> selfAddr,
        "id2203.project.bootstrap-address" -> intToServerAddress(1))
    }
    StartNode(selfAddr, Init.none[ParentComponent], conf);
  }

  val startClientOp = Op { self: Integer =>
    val selfAddr = intToClientAddress(self)
    val conf = Map(
      "id2203.project.address" -> selfAddr,
      "id2203.project.bootstrap-address" -> intToServerAddress(1))
    StartNode(selfAddr, Init.none[ScenarioAtomicClient], conf);
  }

  def scenario(servers: Int): JSimulationScenario = {

    //val networkSetup = raise(1, setUniformLatencyNetwork()).arrival(constant(0))
    val startCluster = raise(servers, startServerOp, 1.toN).arrival(constant(1.second))
    val startClients = raise(1, startClientOp, 1.toN).arrival(constant(1.second))

    //startCluster andThen
    //    networkSetup andThen
    //      0.seconds afterTermination startCluster andThen
    //      10.seconds afterTermination startClients andThen
    //      100.seconds afterTermination Terminate
    startCluster andThen
      10.seconds afterTermination startClients andThen
      100.seconds afterTermination Terminate
  }

}
