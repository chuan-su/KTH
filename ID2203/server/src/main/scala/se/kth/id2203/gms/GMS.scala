package se.kth.id2203.gms

import se.sics.kompics.sl.Port

class GMS extends Port {
  request[GMSInit]
  indication[GMSView]
}
