package se.kth.id2203.infrastructure.PerfectLink

import se.kth.id2203.networking.{NetAddress, NetHeader, NetMessage}
import se.sics.kompics.KompicsEvent
import se.sics.kompics.network.{Network, Transport}
import se.sics.kompics.sl.{ComponentDefinition, NegativePort, PositivePort, handle}

class PerfectLinkC extends ComponentDefinition {
  val pLink: NegativePort[PerfectLink] = provides[PerfectLink]
  val net: PositivePort[Network] = requires[Network]

  val self: NetAddress = cfg.getValue[NetAddress]("id2203.project.address")

  pLink uponEvent {
    case PL_Send(dest: NetAddress, payload: KompicsEvent) => handle {
      val header = NetHeader(self, dest, Transport.TCP)
      trigger(NetMessage(header, PL_Deliver(self, payload)) -> net)
    }
  }

  net uponEvent {
    case NetMessage(_, content: PL_Deliver) => handle {
      trigger(content -> pLink);
    }
  }

}
