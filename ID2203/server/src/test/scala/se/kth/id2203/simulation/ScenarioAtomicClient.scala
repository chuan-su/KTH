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

import scala.util
import se.kth.id2203.kvstore.OpCode.OpCode

import scala.util.Random
//import java.util.Queue

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

case class OperationItem(id: UUID, key: String, value: Option[Any], old_val: Option[Any], operator: String,method: String, statusCode: OpCode)
class ScenarioAtomicClient extends ComponentDefinition {
  //******* Ports ******
  val net = requires[Network]
  val timer = requires[Timer]
  //******* Fields ******
  val self = cfg.getValue[NetAddress]("id2203.project.address")
  val server = cfg.getValue[NetAddress]("id2203.project.bootstrap-address")
  private val pending = mutable.Map.empty[UUID, (String, String)]
  val res = SimulationResultSingleton.getInstance()

  val queue = mutable.Queue.empty[OperationItem]
  var messages = 0
  val send_queue = mutable.Queue.empty[Operation]
  //******* Handlers ******
  ctrl uponEvent {
    case _: Start => handle {
      messages = SimulationResult[Int]("messages")
      res.put("linear", "false")
      var r = new Random()
      for (i <- 0 to messages) {
        val op = new Put(s"$i", i.toString)
        send_queue.enqueue(op)

        queue.enqueue(OperationItem(op.id, op.key, Some(i.toString), None, "put", "inv", null))
        pending += (op.id -> (op.key, "Put"))

        val op2 = new Get(s"$i")
        send_queue.enqueue(op2)

        pending += (op2.id -> (op2.key, "Get"))
        queue.enqueue(OperationItem(op2.id, op2.key, Some(i.toString), None, "get", "inv", null))

        val i2 = r.nextInt(100).toString
        val op3 = new CAS(s"$i", i.toString, i2)
        send_queue.enqueue(op3)
        pending += (op3.id -> (op3.key, "CAS"))
        queue.enqueue(OperationItem(op3.id, op3.key, Some(i2.toString), Some(i.toString), "cas", "inv", null))

        while(send_queue.nonEmpty){
          val op_i = send_queue.dequeue()
          val routeMsg = RouteMsg(op_i.key, op_i); // don't know which partition is responsible, so ask the bootstrap server to forward it
          trigger(NetMessage(self, server, routeMsg) -> net)
        }
      }
    }
  }

  //Checking linearizeability with the modified Wing & Gong algorithm found in "Testing for Linearizeability" by Lowe(2010)
  def checkLinearizeability(h: mutable.Queue[OperationItem], S: mutable.Map[String, OperationItem]): Boolean ={
    if(h.isEmpty){
      return true
    }
    else{
      var minQ = getMinimalOperation(h)
      for(q <- minQ){
        var op_res = findRes(q._2.id, h)
        var S_res = simulateRes(q._2, S)
        var newS = S + q

        var h2 = customRemove(h, q)
        if(S_res == op_res && checkLinearizeability( h2, newS)){
          return true
        }
        else{
          //Implicit undo of S, and restoration of h for next iteration
        }
      }
    }
    false
  }

  def getMinimalOperation(h: mutable.Queue[OperationItem]): mutable.Map[String, OperationItem] ={
    var minOps = mutable.Map.empty[String, OperationItem]
    for(op <- h){
      if(op.method == "res"){
        return minOps
      }
      else{
        if(findRes(op.id, h)!= null){
          minOps.put(op.key+op.method, op)
        }
      }
    }
    minOps
  }

  def findRes(id: UUID, h: mutable.Queue[OperationItem]): OperationItem = {
    for(op <- h){
      if(op.method == "res" && op.id == id) return op
    }
    null
  }

  def simulateRes(op: OperationItem, S: mutable.Map[String, OperationItem]): OperationItem = {
    var res = None:Option[Any]
    val key = op.key
    var value = op.value
    val id2 = op.id
    val old_val = op.old_val
    val operator = op.operator
    val method = op.method
    var statusCode = op.statusCode
    operator match{
      case "get" =>{
        res = S.get(op.key+op.method)
        if(res.isDefined){
          value = res
        }
      }case "put" =>{
        S.put(op.key+op.method, op)
        statusCode = OpCode.Ok
      }case "cas" =>{
        val old2 = S.get(key)
        if(old2.isDefined && old_val == old2){
          S.put(op.key+op.method, op)
          statusCode = OpCode.Ok
        } else{
          statusCode = OpCode.NotModified
        }
      }
        return OperationItem(id2, key, value, old_val, operator, method, statusCode)
    }

    null
  }
  def customRemove(h: mutable.Queue[OperationItem], item: (String, OperationItem)):mutable.Queue[OperationItem]={
    var h2 = mutable.Queue.empty[OperationItem]
    for(q <- h){
      if(q.id != item._2.id){
        h2.enqueue(q)
      }
    }
    h2
  }

  net uponEvent {
    case NetMessage(header, or @ OpResponse(value, id, status)) => handle {
      logger.debug(s"Got OpResponse: $or")
      pending.remove(id) match {
        case Some((key, op)) => {
          op match {
            case "Put" => {
              queue.enqueue(OperationItem(or.id, key, None, None, "put", "res", status))
            }
            case "Get" => {
              queue.enqueue(OperationItem(or.id, key, Some(value), None, "get", "res", status))
            }
            case "CAS" => {
              queue.enqueue(OperationItem(or.id, key, None, None, "cas", "res", status))
            }
          }
        }
        case None => logger.warn("ID $id was not pending! Ignoring response.")
      }
      if (send_queue.isEmpty && queue.size >= messages * 6) {
        if (checkLinearizeability(queue, mutable.Map.empty[String, OperationItem])) res.put("linear", "true")
      }
    }

  }
}
