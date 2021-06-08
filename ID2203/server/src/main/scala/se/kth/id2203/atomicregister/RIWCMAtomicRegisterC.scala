package se.kth.id2203.atomicregister

import se.kth.id2203.infrastructure.BestEffortBroadcast.{BEB_Broadcast, BEB_Deliver, BestEffortBroadcast}
import se.kth.id2203.infrastructure.PerfectLink.{PL_Deliver, PL_Send, PerfectLink}
import se.kth.id2203.networking.NetAddress
import se.kth.id2203.peer.{Peer, ReConfig}
import se.sics.kompics.sl.{ComponentDefinition, handle}

class RIWCMAtomicRegisterC extends ComponentDefinition {

  val self: NetAddress = cfg.getValue[NetAddress]("id2203.project.address")
  // subscriptions
  val nnar = provides[AtomicRegister]

  val pLink = requires[PerfectLink]
  val beb = requires[BestEffortBroadcast]
  val peer = requires[Peer]

  // state and init
  var store: Map[String, (RIWCMState, Item)] = Map.empty

  var majority = 0

  peer uponEvent {
    case config: ReConfig => handle {
      majority = config.majority
      log.info("========== AtomicRegister majority {}!", majority)
    }
  }

  nnar uponEvent {
    case AR_Read_Request(key) => handle {
      store.get(key) match  {
        case Some((state: RIWCMState, _: Item)) => {
          state.acks = 0
          state.reading = true
          state.readlist = Map.empty
        }
        case None =>
          store += (key -> (RIWCMState(reading = true), new Item))
      }
      trigger(BEB_Broadcast(READ(key)) -> beb)
    }
    case AR_Write_Request(key, wval) => handle {
      store.get(key) match  {
        case Some((state: RIWCMState, _: Item)) => {
          state.acks = 0
          state.readlist = Map.empty
          state.writeval = Some(wval)
        }
        case None =>
          store += (key -> (RIWCMState(writeval = Some(wval)), new Item))
      }
      trigger(BEB_Broadcast(READ(key)) -> beb)
    }
    case AR_CAS_Request(key, refval, wval) => handle {
      store.get(key) match  {
        case Some((state: RIWCMState, _: Item)) => {
          state.acks = 0
          state.readlist = Map.empty
          state.writeval = Some(wval)
          state.cas = true
          state.casRef = Some(refval)
        }
        case None =>
          store += (key -> (RIWCMState(writeval = Some(wval), cas = true, casRef = Some(refval)), new Item))
      }
      trigger(BEB_Broadcast(READ(key)) -> beb)
    }
  }

  beb uponEvent {
    case BEB_Deliver(src, READ(key)) => handle {
      if(store.get(key).isEmpty) {
        store += (key -> (new RIWCMState, new Item))
      }
      val (_, item) = store(key)
      trigger(PL_Send(src, VALUE(item.ts, item.wr, key, item.value)) -> pLink)
    }
    case BEB_Deliver(src, w: WRITE) => handle {
      val (_, item) = store(w.key)
      val timestamp = (w.ts, w.wr)

      if (item.isTsNewer(timestamp)) {
        item.update(w.writeVal, timestamp)
      }
      trigger(PL_Send(src, ACK(w.key)) -> pLink)
    }
  }

  pLink uponEvent {
    case PL_Deliver(src, v: VALUE) => handle {
      val (state, item) = store(v.key)
      state.readlist += (src -> (v.ts, v.wr, v.value))

      if (state.readlist.size >= majority) {
        val (mts, rr, mv): (Int, Int, Option[Any])  = state.highestRead

        state.readval = mv
        state.readlist = Map.empty

        if (state.cas && !state.readval.contains(state.casRef.get)) {
          state.cas = false
          state.casRef = None
          trigger(AR_CAS_Response(v.key, updated = false) -> nnar)
        } else if (state.reading) {
          trigger(BEB_Broadcast(WRITE(mts, rr, v.key, state.readval)) -> beb)
        } else{
          trigger(BEB_Broadcast(WRITE(mts + 1, item.wr, v.key, state.writeval)) -> beb)
        }
      }
    }

    case PL_Deliver(src, v: ACK) => handle {
      val (state, _) = store(v.key)
      state.acks = state.acks + 1

      if (state.acks >= majority) {
        state.acks = 0
        if (state.cas) {
          state.cas = false
          state.casRef = None
          trigger(AR_CAS_Response(v.key, updated = true) -> nnar)
        } else if (state.reading) {
          state.reading = false
          trigger(AR_Read_Response(v.key, state.readval) -> nnar)
        } else {
          trigger(AR_Write_Response(v.key) -> nnar)
        }
      }
    }
  }
}
