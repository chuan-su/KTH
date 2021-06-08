package se.kth.id2203

import se.kth.id2203.networking.NetAddress
import se.sics.kompics.KompicsEvent
import se.sics.kompics.timer.{ScheduleTimeout, Timeout}

package object epfd {
  case class EPFDInit(allProcess: Set[NetAddress]) extends KompicsEvent
  case class Suspect(process: NetAddress) extends KompicsEvent
  case class Restore(process: NetAddress) extends KompicsEvent

  //Custom messages to be used in the internal component implementation
  case class CheckTimeout(timeout: ScheduleTimeout) extends Timeout(timeout)
  case class HeartbeatReply(seq: Int) extends KompicsEvent
  case class HeartbeatRequest(seq: Int) extends KompicsEvent
}
