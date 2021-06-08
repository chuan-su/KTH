package se.kth.id2203.infrastructure.BestEffortBroadcast

import se.sics.kompics.sl.Port

class BestEffortBroadcast extends Port {
  indication[BEB_Deliver]
  request[BEB_Broadcast]
}
