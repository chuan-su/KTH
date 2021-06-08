package id2221

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.spark.graphx.{EdgeDirection, Graph, GraphLoader, VertexId}
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object CommunityDetect {

  type Msg = Map[VertexId, Set[VertexId]]
  type State = (Msg, Msg, Clique)
  type Clique = Set[VertexId]

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("ID2221").setMaster("local[*]")
    val sc = new SparkContext(conf)

    val graph: Graph[State, _]= GraphLoader.edgeListFile(sc, "dataset/twitter")
                .mapVertices((_, _) => (Map(), Map(), Set()))

    val initialMsg: Msg = Map[VertexId, Set[VertexId]]()

    val result = graph.pregel(initialMsg, Int.MaxValue, EdgeDirection.Out)(
      // Apply
      (me, state, msg) => { // vertex program
        // update state
        val oldState = state._1
        val newState = mergeMsg(oldState, msg)
        val clique = findMaximalClique(me, newState)
        if (clique.nonEmpty) {

          KafkaProducer.send(new ProducerRecord[String, String]("community", me.toString, clique.mkString(",")));
        }
        (newState, oldState, clique)
      },
      // Scatter
      triplet => { // outgoing message
        if (triplet.srcAttr._1.nonEmpty && triplet.srcAttr._1.size == triplet.srcAttr._2.size) {
          Iterator.empty
        } else {
          Iterator((triplet.srcId, Map(triplet.dstId -> triplet.dstAttr._1.keys.toSet)))
        }
      },
      // Gather
      mergeMsg
    )
    result.mapVertices[Clique]{
      case (_:VertexId, (_:Msg, _:Msg, c: Clique)) => c}
      .vertices
      .filter { case (_: VertexId, c: Clique) => c.nonEmpty }
      .coalesce(1)
      .saveAsTextFile("result/community")
  }

  def mergeMsg(m1: Msg, m2: Msg): Msg = {
    (m1 /: m2) { case (map, (k, v)) =>
        map + (k -> v.union(map.getOrElse(k, Set.empty[VertexId])))
    }
  }
  def findMaximalClique(me: VertexId, msg: Msg): Clique = {
    val result = ArrayBuffer[Clique]()

    bronKerbosch(Set(), mutable
      .Set[VertexId](msg.keySet.toArray:_*), mutable.Set[VertexId](), msg, result)

    result.maxBy(clique => clique.size)
  }

  def bronKerbosch(R: Clique, P: mutable.Set[VertexId], X: mutable.Set[VertexId], neighborhood: Msg, cliques: ArrayBuffer[Clique]): Unit = {
    if (P.isEmpty && X.isEmpty) {
      cliques.append(R)
      return
    }
    for (v <- P) {
      if (neighborhood.contains(v)) {
        val neighbors = neighborhood(v)
        bronKerbosch(R + v, P.intersect(neighbors), X.intersect(neighbors), neighborhood, cliques)
        X += v
      }
      P -= v
    }
  }
}
