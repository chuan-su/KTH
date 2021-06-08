package se.kth.id2203.atomicregister

import se.kth.id2203.networking.NetAddress

case class RIWCMState(var reading: Boolean = false,
                      var writeval: Option[Any] = None,
                      var readval: Option[Any] = None,
                      var readlist: Map[NetAddress, (Int, Int, Option[Any])] = Map.empty,
                      var acks: Int = 0,
                      var cas: Boolean = false,
                      var casRef: Option[Any] = None) {

  def highestRead: (Int, Int, Option[Any]) = readlist.maxBy(_._2._1)._2
}

class Item(var value: Option[Any] = None) {
  /**
    * This augments tuples with comparison operators implicitly, which you can use in your code.
    * examples: (1,2) > (1,4) yields 'false' and  (5,4) <= (7,4) yields 'true'
    */
  implicit def addComparators[A](x: A)(implicit o: math.Ordering[A]): o.Ops = o.mkOrderingOps(x)
  // Timestamp
  var (ts, wr) = (0, 0)

  def isTsNewer(timestamp: (Int, Int)): Boolean = {
    (ts, wr) <= timestamp
  }

  def update(writeVal: Option[Any], timestamp: (Int, Int)): Unit = {
    value = writeVal
    ts = timestamp._1
    wr = timestamp._2
  }
}
