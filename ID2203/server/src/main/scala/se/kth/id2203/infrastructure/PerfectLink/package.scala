package se.kth.id2203.infrastructure

import se.kth.id2203.networking.NetAddress
import se.sics.kompics.KompicsEvent

package object PerfectLink {
  case class PL_Send(dest: NetAddress, payload: KompicsEvent) extends KompicsEvent
  case class PL_Deliver(src: NetAddress, payload: KompicsEvent) extends KompicsEvent
}
