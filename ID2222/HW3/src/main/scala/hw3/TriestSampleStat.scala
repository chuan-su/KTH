package hw3

import org.apache.spark.graphx.{Edge, VertexId}

class TriestSampleStat(var sampleSize : Int) extends Serializable{

  var elementCounter: Int = 0
  var globalCounter: Double = 0
  var sample: Set[Edge[Int]] = Set[Edge[Int]]()
  val localCounter: scala.collection.mutable.Map[VertexId, Double] = scala.collection.mutable.Map()

  def increaseElementCounter(): Unit = {
    elementCounter = elementCounter + 1
  }

  def updateGlobalCounter(count: Double): Unit = {
    globalCounter = globalCounter + count
  }

  def updateLocalCounter(vertexId: VertexId, count: Double): Unit = {
    val originalCount : Double = localCounter.getOrElse(vertexId, 0.0)
    localCounter.put(vertexId, originalCount + count)
  }

  def addToSample(edge: Edge[Int]): Unit = {
    sample = sample.union(Set(edge))
  }

  def removeFromSample(edge: Edge[Int]): Unit = {
    sample = sample - edge
  }

  def triangleEstimate() :Double = {
    val numerator = (elementCounter * (elementCounter - 1) * (elementCounter - 2)).toDouble
    val denominator = (sampleSize * (sampleSize - 1 ) * ( sampleSize - 2)).toDouble

    val epsilon = scala.math.max(1, numerator/denominator)
    val estimate = epsilon * globalCounter

    estimate
  }

  def weightToIncreaseForImpr() :Double = {
    val numerator = ((elementCounter - 1) * (elementCounter - 2)).toDouble
    val denominator = (sampleSize * (sampleSize - 1)).toDouble

    val increasedWeight = scala.math.max(1, numerator/denominator)

    increasedWeight
  }
}
