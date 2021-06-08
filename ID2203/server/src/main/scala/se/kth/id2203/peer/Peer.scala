package se.kth.id2203.peer

import se.kth.id2203.gms.GMSView
import se.sics.kompics.sl.Port

class Peer extends Port {
  request[PeerRequest]
  request[GMSView]
  indication[PeerResponse]
  indication[ReConfig]
}
