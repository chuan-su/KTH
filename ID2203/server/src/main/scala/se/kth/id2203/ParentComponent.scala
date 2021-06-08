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
package se.kth.id2203;

import se.kth.id2203.atomicregister.{AtomicRegister, RIWCMAtomicRegisterC}
import se.kth.id2203.bootstrapping._
import se.kth.id2203.epfd.{EPFD, EventuallyPerfectFailureDetector}
import se.kth.id2203.gms.{GMS, GMSC}
import se.kth.id2203.infrastructure.BestEffortBroadcast.{BasicBroadcast, BestEffortBroadcast}
import se.kth.id2203.infrastructure.PerfectLink.{PerfectLink, PerfectLinkC}
import se.kth.id2203.kvstore.KVService
import se.kth.id2203.networking.NetAddress
import se.kth.id2203.overlay._
import se.kth.id2203.peer.{Peer, PeerC}
import se.sics.kompics.sl._
import se.sics.kompics.{Channel, Component, Init}
import se.sics.kompics.network.Network
import se.sics.kompics.timer.Timer;

class ParentComponent extends ComponentDefinition {

  //******* Ports ******
  val net = requires[Network]
  val timer = requires[Timer]
  //******* Children ******
  val overlay = create(classOf[VSOverlayManager], Init.NONE)
  val kv = create(classOf[KVService], Init.NONE)

  //******* Infrastructure *****
  val perfectLink: Component = create(classOf[PerfectLinkC], Init.NONE)
  val bebBroadcast: Component = create(classOf[BasicBroadcast], Init.NONE)
  val atomicRegister: Component = create(classOf[RIWCMAtomicRegisterC], Init.NONE)
  val epfd: Component = create(classOf[EPFD], Init.NONE)
  val gms: Component = create(classOf[GMSC], Init.NONE)
  val peer: Component = create(classOf[PeerC], Init.NONE)

  val boot = cfg.readValue[NetAddress]("id2203.project.bootstrap-address") match {
    case Some(_) => create(classOf[BootstrapClient], Init.NONE); // start in client mode
    case None    => create(classOf[BootstrapServer], Init.NONE); // start in server mode
  }

  {
    connect[Timer](timer -> boot)
    connect[Network](net -> boot)
    // Overlay
    connect(Bootstrapping)(boot -> overlay)
    connect[Network](net -> overlay)
    connect(gms.getPositive(classOf[GMS]), overlay.getNegative(classOf[GMS]), Channel.TWO_WAY)
    connect(epfd.getPositive(classOf[EventuallyPerfectFailureDetector]), overlay.getNegative(classOf[EventuallyPerfectFailureDetector]), Channel.TWO_WAY)
    // KV
    connect(Routing)(overlay -> kv)
    connect[Network](net -> kv)
    connect(peer.getPositive(classOf[Peer]), kv.getNegative(classOf[Peer]), Channel.TWO_WAY)
    // GMS
    connect(epfd.getPositive(classOf[EventuallyPerfectFailureDetector]), gms.getNegative(classOf[EventuallyPerfectFailureDetector]), Channel.TWO_WAY)
    // EPFD
    connect(timer, epfd.getNegative(classOf[Timer]), Channel.TWO_WAY)
    connect(perfectLink.getPositive(classOf[PerfectLink]), epfd.getNegative(classOf[PerfectLink]), Channel.TWO_WAY)
    // Peer
    connect(atomicRegister.getPositive(classOf[AtomicRegister]), peer.getNegative(classOf[AtomicRegister]), Channel.TWO_WAY)
    connect(gms.getPositive(classOf[GMS]), peer.getNegative(classOf[GMS]), Channel.TWO_WAY)
    // AtomicRegister
    connect(peer.getPositive(classOf[Peer]), atomicRegister.getNegative(classOf[Peer]), Channel.TWO_WAY)
    connect(bebBroadcast.getPositive(classOf[BestEffortBroadcast]), atomicRegister.getNegative(classOf[BestEffortBroadcast]), Channel.TWO_WAY)
    connect(perfectLink.getPositive(classOf[PerfectLink]), atomicRegister.getNegative(classOf[PerfectLink]), Channel.TWO_WAY)
    // Beb
    connect(peer.getPositive(classOf[Peer]), bebBroadcast.getNegative(classOf[Peer]), Channel.TWO_WAY)
    connect(perfectLink.getPositive(classOf[PerfectLink]), bebBroadcast.getNegative(classOf[PerfectLink]), Channel.TWO_WAY)
    // Perfect Link
    connect(net, perfectLink.getNegative(classOf[Network]), Channel.TWO_WAY)
  }
}
