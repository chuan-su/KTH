package se.kth.id2203.epfd

import se.kth.id2203.bootstrapping.Bootstrapping
import se.kth.id2203.infrastructure.PerfectLink.{PL_Deliver, PL_Send, PerfectLink}
import se.kth.id2203.networking.NetAddress
import se.sics.kompics.Start
import se.sics.kompics.sl.{ComponentDefinition, handle}
import se.sics.kompics.timer.{ScheduleTimeout, Timer}

class EPFD extends ComponentDefinition {

  // EPFD subscriptions
  val boot = requires(Bootstrapping)
  val timer = requires[Timer]
  val pLink = requires[PerfectLink]
  val epfd = provides[EventuallyPerfectFailureDetector]

  // config parameters
  val self = cfg.getValue[NetAddress]("id2203.project.address")
  var topology = Set.empty[NetAddress]

  val delta = cfg.getValue[Long]("id2203.project.keepAlivePeriod")

  //mutable state
  var period = cfg.getValue[Long]("id2203.project.keepAlivePeriod")
  var alive = Set.empty[NetAddress]
  var suspected = Set.empty[NetAddress]
  var seqnum = 0

  def startTimer(delay: Long): Unit = {
    val scheduledTimeout = new ScheduleTimeout(delay)
    scheduledTimeout.setTimeoutEvent(CheckTimeout(scheduledTimeout))
    trigger(scheduledTimeout -> timer)
  }

  ctrl uponEvent {
    case _: Start => handle {
      startTimer(period)
    }
  }

  epfd uponEvent {
    case EPFDInit(allProcesses) => handle {
      topology ++= allProcesses
      alive ++= allProcesses
    }
  }

  timer uponEvent {
    case CheckTimeout(_) => handle {
      if (alive.intersect(suspected).nonEmpty) {
        period = period + delta
      }
      seqnum = seqnum + 1

      for (p <- topology) {
        if (!alive.contains(p) && !suspected.contains(p)) {
          suspected -= p
          trigger(Suspect(p) -> epfd)
        } else if (alive.contains(p) && suspected.contains(p)) {
          suspected -= p
          trigger(Restore(p) -> epfd)
        }
        trigger(PL_Send(p, HeartbeatRequest(seqnum)) -> pLink)
      }
      alive = Set.empty[NetAddress]
      startTimer(period)
    }
  }

  pLink uponEvent {
    case PL_Deliver(src, HeartbeatRequest(seq)) => handle {
      trigger(PL_Send(src, HeartbeatReply(seq)) -> pLink)
    }
    case PL_Deliver(src, HeartbeatReply(seq)) => handle {
      if (seq == seqnum || suspected(src)) {
        alive += src
      }
    }
  }
}
