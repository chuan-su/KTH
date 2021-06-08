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

import java.util.UUID

import se.kth.id2203.kvstore.OpCode.OpCode
import se.kth.id2203.kvstore._
import se.kth.id2203.networking._
import se.kth.id2203.overlay.RouteMsg
import se.sics.kompics.sl._
import se.sics.kompics.Start
import se.sics.kompics.network.Network
import se.sics.kompics.simulator.result.SimulationResultSingleton
import se.sics.kompics.timer.Timer
import se.sics.kompics.sl.simulator.SimulationResult

import collection.mutable

class ScenarioClient extends ComponentDefinition {

  //******* Ports ******
  val net = requires[Network]
  val timer = requires[Timer]
  //******* Fields ******
  val self = cfg.getValue[NetAddress]("id2203.project.address")
  val server = cfg.getValue[NetAddress]("id2203.project.bootstrap-address")
  private val pending = mutable.Map.empty[UUID, (String, String)]
  val res = SimulationResultSingleton.getInstance()
  //******* Handlers ******
  ctrl uponEvent {
    case _: Start => handle {
      val messages = SimulationResult[Int]("messages")
      for (i <- 0 to messages) {
        val op = new Op( s"test$i")
        val routeMsg = RouteMsg(op.key, op); // don't know which partition is responsible, so ask the bootstrap server to forward it
        trigger(NetMessage(self, server, routeMsg) -> net)
        pending += (op.id -> (op.key, "Op"))
        logger.info("Sending {}", op)
        res.put(op.key, "Sent")
      }
      for (i <- 0 to messages) {
        val op = new Put(s"$i", i.toString)
        val routeMsg = RouteMsg(op.key, op); // don't know which partition is responsible, so ask the bootstrap server to forward it
        trigger(NetMessage(self, server, routeMsg) -> net)
        pending += (op.id -> (op.key, "Put"))
        logger.info("Sending PUT{}", op)
        res.put("put"+op.key, "Sent")
      }
    }
  }

  net uponEvent {
    case NetMessage(header, or @ OpResponse(value, id, status)) => handle {
      logger.debug(s"Got OpResponse: $or")
      pending.remove(id) match {
        case Some((key, op)) =>
          op match {
            case "Op" => {
              res.put(key, value.getOrElse(status.toString))
            }
            case "Put" =>{
              res.put("put"+key,"PutSuccess")
              val op = new Get(key)
              val routeMsg = RouteMsg(op.key, op)
              trigger(NetMessage(self, server, routeMsg) -> net)
              res.put("get"+op.key, "Sent")
              pending += (op.id -> (op.key, "Get"))
            }
            case "Get" =>{
              res.put("get"+key, value.getOrElse(status.toString))
              assert(value.getOrElse(None) != None)
              val op = new CAS(key, value.get.toString, "0")
              val routeMsg = RouteMsg(op.key, op)
              trigger(NetMessage(self, server, routeMsg) -> net)
              res.put("cas"+op.key, "Sent")
              pending += (op.id -> (op.key, "CAS"))
            }
            case "CAS" =>{
              SimulationResult += (s"cas$key" -> value.getOrElse(status.toString))
            }
          }
          //res.put(key, value.getOrElse(status.toString))
        case None      => logger.warn("ID $id was not pending! Ignoring response.")
      }
    }
//    case NetMessage(header, or @ PutResponse(value, id, status)) => handle {
//      logger.debug(s"Got PutResponse: $or")
//      pending.remove(id) match {
//        case Some(key) =>
//          res.put("put"+key,"PutSuccess")
//          val op = new Get(key)
//          val routeMsg = RouteMsg(op.key, op)
//          trigger(NetMessage(self, server, routeMsg) -> net)
//          res.put("get"+op.key, "Sent")
//          pending += (op.id -> op.key)
//        case None      => logger.warn("ID $id was not pending! Ignoring response.")
//      }
//    }
//    case NetMessage(header, or @ GetResponse(value, id, status)) => handle {
//      logger.debug(s"Got GetResponse: $or")
//      pending.remove(id) match {
//        case Some(key) =>
//          res.put("get"+key, value.getOrElse(status.toString))
//      assert(value.getOrElse(None) != None)
//      val op = new CAS(key, value.get.toString, "0")
//      val routeMsg = RouteMsg(op.key, op)
//      trigger(NetMessage(self, server, routeMsg) -> net)
//      res.put("cas"+op.key, "Sent")
//      pending += (op.id -> op.key)
//        case None      => logger.warn("ID $id was not pending! Ignoring response.")
//      }
//    }
//    case NetMessage(header, or @ CASResponse(value, id, status)) => handle {
//      logger.debug(s"Got CASResponse: $or")
//      pending.remove(id) match {
//        case Some(key) =>
//          SimulationResult += (s"cas$key" -> value.getOrElse(status.toString))
//        case None      => logger.warn("ID $id was not pending! Ignoring response.")
//      }
//    }

  }
}
