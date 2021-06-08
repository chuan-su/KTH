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
package se.kth.id2203.kvstore

import java.util.concurrent.ConcurrentHashMap

import se.kth.id2203.networking._
import se.kth.id2203.overlay.Routing
import se.kth.id2203.peer.{Peer, PeerRequest, PeerResponse}
import se.sics.kompics.sl._
import se.sics.kompics.network.Network

class KVService extends ComponentDefinition {

  //******* Ports ******
  val net = requires[Network]
  val peer = requires[Peer]
  val route = requires(Routing)
  //******* Fields ******
  val self = cfg.getValue[NetAddress]("id2203.project.address")
  var activeOps = new ConcurrentHashMap[String, ClientOperation]()

  //******* Handlers ******
  net uponEvent {
    case NetMessage(header, operation: Operation) => handle {
      log.info("========== OPERATION request {}!", operation)
      if (activeOps.contains(operation.key)) {
        trigger(NetMessage(self, header.src, OpResponse(None, operation.id, OpCode.StoreBusy)) -> net)
      } else {
        activeOps.put(operation.key, ClientOperation(header.src, operation))
        trigger(PeerRequest(operation) -> peer)
      }
    }
  }

  peer uponEvent {
    case PeerResponse(key, value, updated) => handle {
      val cop: ClientOperation = activeOps.remove(key)
      log.info("========== OPERATION reply {}!", cop.op)
      cop.op match {
        case get: Get =>
          val opCode = if (value.isDefined) OpCode.Ok else OpCode.NotFound
          trigger(NetMessage(self, cop.clientAddress, get.response(value, opCode)) -> net)
        case put: Put =>
          trigger(NetMessage(self, cop.clientAddress, put.response(OpCode.Ok)) -> net)
        case cas: CAS =>
          val opCode = if (updated) OpCode.Ok else OpCode.NotModified
          trigger(NetMessage(self, cop.clientAddress, cas.response(opCode)) -> net)
        case op: Op =>
          trigger(NetMessage(self, cop.clientAddress, op.response(OpCode.Ok)) -> net)
      }
    }
  }
}
