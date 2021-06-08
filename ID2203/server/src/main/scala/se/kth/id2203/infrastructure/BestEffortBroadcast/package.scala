package se.kth.id2203.infrastructure

import se.kth.id2203.networking.NetAddress
import se.sics.kompics.KompicsEvent

package object BestEffortBroadcast {
  case class BEB_Deliver(source: NetAddress, payload: KompicsEvent) extends KompicsEvent
  case class BEB_Broadcast(payload: KompicsEvent) extends KompicsEvent
}
