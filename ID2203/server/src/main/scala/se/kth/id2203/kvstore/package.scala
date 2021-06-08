package se.kth.id2203

import se.kth.id2203.networking.NetAddress

package object kvstore {
  case class ClientOperation(clientAddress: NetAddress, op: Operation)
}
