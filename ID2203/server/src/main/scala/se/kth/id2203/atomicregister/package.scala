package se.kth.id2203

import se.sics.kompics.KompicsEvent

package object atomicregister {
  case class AR_Read_Request(key: String) extends KompicsEvent
  case class AR_Read_Response(key: String, value: Option[Any]) extends KompicsEvent
  case class AR_Write_Request(key: String, value: Any) extends KompicsEvent
  case class AR_Write_Response(key: String) extends KompicsEvent
  case class AR_CAS_Request(key: String, referenceValue: Any, newValue: Any) extends KompicsEvent
  case class AR_CAS_Response(key: String, updated: Boolean) extends KompicsEvent

  //The following events are to be used internally by the Atomic Register implementation below
  case class READ(key: String) extends KompicsEvent
  case class VALUE(ts: Int, wr: Int, key:String, value: Option[Any]) extends KompicsEvent
  case class WRITE(ts: Int, wr: Int, key:String, writeVal: Option[Any]) extends KompicsEvent
  case class ACK(key: String) extends KompicsEvent
}
