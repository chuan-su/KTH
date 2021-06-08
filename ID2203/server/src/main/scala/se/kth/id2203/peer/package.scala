package se.kth.id2203

import se.kth.id2203.kvstore.Operation
import se.kth.id2203.networking.NetAddress
import se.sics.kompics.KompicsEvent

package object peer {
  case class PeerRequest(operation: Operation) extends KompicsEvent
  case class PeerResponse(key: String, value: Option[Any] = None, updated: Boolean = false) extends KompicsEvent

  case class ReConfig(topology: Set[NetAddress]) extends KompicsEvent {
    def majority :Int = (topology.size / 2) + 1
  }
}
