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
package se.kth.id2203.overlay

import se.kth.id2203.bootstrapping._
import se.kth.id2203.epfd._
import se.kth.id2203.gms.{GMS, GMSInit}
import se.kth.id2203.networking._
import se.sics.kompics.sl._
import se.sics.kompics.network.Network
import se.sics.kompics.timer.Timer

import util.Random;

/**
 * The V(ery)S(imple)OverlayManager.
 * <p>
 * Keeps all nodes in a single partition in one replication group.
 * <p>
 * Note: This implementation does not fulfill the project task. You have to
 * support multiple partitions!
 * <p>
 * @author Lars Kroll <lkroll@kth.se>
 */
class VSOverlayManager extends ComponentDefinition {

  //******* Ports ******
  val route = provides(Routing)
  val boot = requires(Bootstrapping)
  val net = requires[Network]
  val timer = requires[Timer]
  val gms = requires[GMS]
  val epfd = requires[EventuallyPerfectFailureDetector]

  //******* Fields ******
  val self = cfg.getValue[NetAddress]("id2203.project.address")
  val replicationDegree = cfg.getValue[Int]("id2203.project.replicationDegree")
  private var lut: Option[LookupTable] = None
  private var suspect = Set.empty[NetAddress]

  //******* Handlers ******
  boot uponEvent {
    case GetInitialAssignments(nodes) => handle {
      log.info("Generating LookupTable...")
      val lut_propose = LookupTable.generate(nodes, replicationDegree)
      logger.debug(s"Generated assignments:\n $lut")
      trigger (new InitialAssignments(lut_propose) -> boot)
    }
    case Booted(assignment: LookupTable) => handle {
      log.info("Got NodeAssignment, overlay ready.")
      lut = Some(assignment)
      trigger(EPFDInit(assignment.getNodes()) -> epfd)
      trigger(GMSInit(assignment.getPartition(self)) -> gms)
    }
  }

  net uponEvent {
    case NetMessage(header, RouteMsg(key, msg)) => handle {
      val target = correctNodeInPartition(key)
      log.info(s"Forwarding message for key $key to $target")
      trigger(NetMessage(header.src, target, msg) -> net)
    }
    case NetMessage(header, msg: Connect) => handle {
      lut match {
        case Some(l) => {
          log.debug("Accepting connection request from ${header.src}")
          val size = l.getNodes().size
          trigger (NetMessage(self, header.src, msg.ack(size)) -> net)
        }
        case None => log.info("Rejecting connection request from ${header.src}, as system is not ready, yet.");
      }
    }
  }

  route uponEvent {
    case RouteMsg(key, msg) => handle {
      val target = correctNodeInPartition(key)
      log.info(s"Routing message for key $key to $target")
      trigger (NetMessage(self, target, msg) -> net)
    }
  }

  epfd uponEvent {
    case Suspect(process: NetAddress) => handle {
        suspect += process
    }
    case Restore(process: NetAddress) => handle {
      suspect -= process
    }
  }

  def correctNodeInPartition(key: String): NetAddress = {
    val nodes = lut.get.lookup(key)
    val correctNodes = nodes.filterNot(suspect.contains(_))
    assert(correctNodes.nonEmpty)

    val i = Random.nextInt(correctNodes.size)
    correctNodes.drop(i).head
  }
}
