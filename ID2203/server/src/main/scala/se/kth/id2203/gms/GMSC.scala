package se.kth.id2203.gms

import se.kth.id2203.bootstrapping.Bootstrapping
import se.kth.id2203.epfd.{EventuallyPerfectFailureDetector, Restore, Suspect}
import se.kth.id2203.networking.NetAddress
import se.sics.kompics.sl.{ComponentDefinition, handle}

class GMSC extends ComponentDefinition {
  val boot = requires(Bootstrapping)
  val epfd = requires[EventuallyPerfectFailureDetector]
  val gms = provides[GMS]

  val self: NetAddress = cfg.getValue[NetAddress]("id2203.project.address")
  var partition = Set.empty[NetAddress]
  var topology = Set.empty[NetAddress]

  gms uponEvent {
    case GMSInit(processes: Set[NetAddress]) => handle {
      topology ++= processes
      partition ++= processes
      log.info("========== GMS topology {}!", topology)
      trigger(GMSView(topology) -> gms)
    }
  }

  epfd uponEvent {
    case Suspect(process: NetAddress) => handle {
      if (topology.contains(process)) {
        topology -= process
        log.info("========== Suspect: current topology size {} vs previous {}", topology.size, partition.size)
        trigger(GMSView(topology) -> gms)
      }
    }
    case Restore(process: NetAddress) => handle {
      if (partition.contains(process)) {
        topology += process
        trigger(GMSView(topology) -> gms)
      }
    }
  }
}
