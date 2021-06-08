package hw3

import org.apache.spark.graphx.{Edge, Graph, VertexId, VertexRDD}
import org.apache.spark.rdd.RDD

import scala.util.Random

object TriestImpr {

  def estimateTriangleCount(edge: Edge[Int], stat : TriestSampleStat): Unit = {
      stat.increaseElementCounter()
      updateCounter(edge, stat)

      if (sampleEdge(edge, stat)) {
        stat.addToSample(edge)
      }
  }

  def sampleEdge(edge: Edge[Int], stat: TriestSampleStat): Boolean = {

    if (stat.elementCounter <= stat.sampleSize) {
      return true
    }
    val p: Double = stat.sampleSize.toDouble / stat.elementCounter.toDouble

    if (flipBiasedCoin(p)) {
      val randomEdge: Edge[Int] = stat.sample.toVector(Random.nextInt(stat.sampleSize))
      stat.removeFromSample(randomEdge)
      return true
    }

    false
  }

  def updateCounter(edge: Edge[Int], stat: TriestSampleStat): Unit = {
    val edgeList : RDD[Edge[Int]] = sc.parallelize(stat.sample.toSeq)

    val graph = Graph.fromEdges(edgeList, 1)

    val neighborhoodRDD: VertexRDD[Set[VertexId]] =
      graph.aggregateMessages[Set[VertexId]](
        edgeContext => {
          edgeContext.sendToSrc(Set[VertexId](edgeContext.dstId))
          edgeContext.sendToDst(Set[VertexId](edgeContext.srcId))
        },
        (s1, s2) => s1 ++ s2
      ).filter {
        case (id: VertexId, _) => id == edge.srcId || id == edge.dstId
      }.mapValues[Set[VertexId]](
        (id: VertexId, neighbors: Set[VertexId]) => neighbors - id
      )

    val neighborhoodArray = neighborhoodRDD.map { case (_, s) => s }.collect

    if (neighborhoodArray.length == 2) {
      val sharedNeighbors = neighborhoodArray(0).intersect(neighborhoodArray(1))

      val increaseWeight = stat.weightToIncreaseForImpr()

      sharedNeighbors.foreach(
        (c: VertexId) => {
          stat.updateGlobalCounter(increaseWeight)
          stat.updateLocalCounter(c, increaseWeight)
          stat.updateLocalCounter(edge.srcId, increaseWeight)
          stat.updateLocalCounter(edge.dstId, increaseWeight)
        })
    }
  }

  //returns true if heads
  //has p probablity of being heads
  //heads when random value <= p
  def flipBiasedCoin(p: Double) : Boolean = {
    val randomValue = Random.nextDouble()
    randomValue <= p
  }
}
