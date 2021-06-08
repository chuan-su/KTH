package se.kth.id2203.infrastructure.PerfectLink

import se.sics.kompics.sl.Port

class PerfectLink extends Port {
  request[PL_Send]
  indication[PL_Deliver]
}
