package se.kth.id2203.peer

import se.kth.id2203.atomicregister._
import se.kth.id2203.gms.{GMS, GMSView}
import se.kth.id2203.kvstore._
import se.kth.id2203.networking.NetAddress
import se.sics.kompics.sl.{ComponentDefinition, handle}

class PeerC extends ComponentDefinition {

  val atomicRegister = requires[AtomicRegister]
  val gms = requires[GMS]
  val peer = provides[Peer]

  peer uponEvent {
    case PeerRequest(operation) => handle {
      operation match {
        case get: Get => trigger(AR_Read_Request(get.key) -> atomicRegister)
        case put: Put => trigger(AR_Write_Request(put.key, put.value) -> atomicRegister)
        case cas: CAS => trigger(AR_CAS_Request(cas.key, cas.referenceValue, cas.newValue) -> atomicRegister)
        case op: Op => trigger(PeerResponse(op.key) -> peer)
      }
    }
  }

  atomicRegister uponEvent {
    case AR_Read_Response(key, value) => handle {
      trigger(PeerResponse(key, value) -> peer)
    }
    case AR_Write_Response(key) => handle {
      trigger(PeerResponse(key) -> peer)
    }
    case AR_CAS_Response(key, updated) => handle {
      trigger(PeerResponse(key, None, updated) -> peer)
    }
  }

  gms uponEvent {
    case GMSView(topology) => handle {
      log.info("========== PEER topology {}!", topology)
      trigger(ReConfig(topology) -> peer)
    }
  }
}
