package se.kth.id2203.infrastructure.BestEffortBroadcast

import se.kth.id2203.infrastructure.PerfectLink.{PL_Deliver, PL_Send, PerfectLink}
import se.kth.id2203.networking.NetAddress
import se.kth.id2203.peer.{Peer, ReConfig}
import se.sics.kompics.sl.{ComponentDefinition, handle}

class BasicBroadcast extends ComponentDefinition {
  // subscriptions
  val peer = requires[Peer]
  val pLink = requires[PerfectLink]
  val beb = provides[BestEffortBroadcast]

  // configurations
  val self: NetAddress = cfg.getValue[NetAddress]("id2203.project.address")
  var topology = Set.empty[NetAddress]

  peer uponEvent {
    case ReConfig(processes) => handle {
      topology = processes
      log.info("========== BEB topology {}!", topology)
    }
  }

  beb uponEvent {
    case x: BEB_Broadcast => handle {
      for (p <- topology) {
        trigger(PL_Send(p, x) -> pLink)
      }
    }
  }

  pLink uponEvent {
    case PL_Deliver(src, BEB_Broadcast(payload)) => handle {
      trigger(BEB_Deliver(src, payload) -> beb)
    }
  }
}
