package se.kth.id2203

import se.kth.id2203.networking.NetAddress
import se.sics.kompics.KompicsEvent

package object gms {
  case class GMSInit(topology: Set[NetAddress]) extends KompicsEvent
  case class GMSView(topology: Set[NetAddress]) extends KompicsEvent
}
