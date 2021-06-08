package se.kth.id2203.epfd

import se.sics.kompics.sl.Port

class EventuallyPerfectFailureDetector extends Port {
  request[EPFDInit]
  indication[Suspect]
  indication[Restore]
}
