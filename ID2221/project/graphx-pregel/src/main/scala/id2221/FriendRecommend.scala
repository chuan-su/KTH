package id2221

import java.io._
import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.immutable.{List, Set}
import scala.collection.mutable.{HashSet, ListBuffer, StringBuilder}

// run with:  $SPARK_HOME/bin/spark-submit --class "FriendRecommend" target/scala-2.11/id2221-project_2.11-0.1.jar
object FriendRecommend{

    def main(args: Array[String]) {
        val sparkConf = new SparkConf().setAppName("FriendRecommend").setMaster("local[*]")
        val sc = new SparkContext(sparkConf)
        sc.addJar("/home/haraldng/.ivy2/cache/org.apache.kafka/kafka_2.11/jars/kafka_2.11-1.0.0.jar")
        val file = sc.textFile("../dataset/twitter_medium")
        val edgesRDD: RDD[(VertexId, VertexId)] = file.flatMap(line => line.split("\n")).map(line => (line.split(" ")(0).toLong, line.split(" ")(1).toLong))
        val graph = Graph.fromEdgeTuples(edgesRDD, 1)
        val initial_graph = graph.mapVertices((id, _) => new VertexState(List[(VertexId, Int)](), Set[VertexId](), true, HashSet[VertexId]()))
        val X = 2
        val max_iterations = X + 1
        val init: List[(VertexId, Int)] = List()
        val result_graph = initial_graph.pregel(
                                          init,
                                          max_iterations,
                                          EdgeDirection.Either)(
                                          PregelFunctions.updateState,
                                          PregelFunctions.sendMessages,
                                          PregelFunctions.mergeMessages)
        val results = result_graph.vertices.collect() // TODO: if unidirectional links, create symmetrical tuples
        val filename = "results.txt"
        val pw = new PrintWriter(new File(filename))
        for (v <- results) {
          pw.write(s"node${v._1}: Recommendations=${v._2.friend_recommends.toList}\n")
        }
        pw.close
        println(s"Done! Results can be found in $filename")
    }
}

@SerialVersionUID(123L)
class VertexState(val msg_queue: List[(VertexId, Int)], val friend_recommends: Set[VertexId], val first_step: Boolean, val neighbors: HashSet[VertexId]) extends Serializable

object PregelFunctions{

  def updateState(vertexId: VertexId, state: VertexState, received_msgs: List[(VertexId, Int)]): VertexState = {
    if (state.first_step){
      new VertexState(List((vertexId, 0)), Set[VertexId](), false, HashSet[VertexId]())
    }
    else {
      var new_recommends = Map[VertexId, Int]()
      var new_neighbors = state.neighbors
      for ((src, num_hops) <- received_msgs) {
        if (num_hops == 0){
          new_neighbors += src
        }
        else if (num_hops > 0 && src != vertexId && !new_neighbors.contains(src) && !state.friend_recommends.contains(src)){ // msg not from adjacent connected vertex(i.e. not already a friend)
          new_recommends += (src -> num_hops)
          // sb.append(s"$src $num_hops,")
        }
      }
      val new_friend_recommends =
        if (new_recommends.nonEmpty){
          val sb = new StringBuilder(s"$vertexId:")  // kafka message
          for((k,v) <- new_recommends){
            sb.append(s"$k $v,")
          }
          KafkaProducer.send(new ProducerRecord[String, String]("friend_recommend", null, sb.toString()))
          new_recommends.keySet ++ state.friend_recommends
        } else {
          state.friend_recommends
        }
      val new_msg_queue = received_msgs.map{case (s, n) => (s, n + 1)}
      val new_state = new VertexState(new_msg_queue, new_friend_recommends, false, new_neighbors)
      new_state
    }
  }

  def sendMessages(triplet: EdgeTriplet[VertexState, Int]): Iterator[(VertexId, List[(VertexId, Int)])] = {
    val msgs: List[(VertexId, Int)] = triplet.srcAttr.msg_queue
    if (msgs.isEmpty) Iterator.empty
    else Iterator((triplet.dstId, msgs))
  }

  def mergeMessages(msg1: List[(VertexId, Int)], msg2: List[(VertexId, Int)]): List[(VertexId, Int)] = msg1 ++ msg2
}
