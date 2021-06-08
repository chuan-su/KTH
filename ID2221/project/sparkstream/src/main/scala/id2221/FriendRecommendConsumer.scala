package id2221

import org.apache.kafka.common.serialization.{StringDeserializer, DoubleDeserializer}
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import com.datastax.spark.connector._
import com.datastax.driver.core.{Session, Cluster, Host, Metadata}
import com.datastax.spark.connector.streaming._
import scala.collection.mutable.Map

object FriendRecommendConsumer {
  def main(args: Array[String]) {
    // connect to Cassandra and make a keyspace and table as explained in the document
    val cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
    val session = cluster.connect()
    val cassandra_space = "id2221_space"
    val friend_recommend_topic = "friend_recommend"
    session.execute(s"CREATE KEYSPACE IF NOT EXISTS $cassandra_space WITH REPLICATION = { 'class': 'SimpleStrategy', 'replication_factor' : 1 };")
    session.execute(s"CREATE TABLE IF NOT EXISTS $cassandra_space.$friend_recommend_topic (vertexid bigint PRIMARY KEY, count int, avg float, recommendations varchar);")

    // make a connection to Kafka and read (key, value) pairs from it
    val conf = new SparkConf().setMaster("local[2]").setAppName("ID2221")
    val ssc = new StreamingContext(conf, Seconds(1))
    ssc.sparkContext.setLogLevel("ERROR")
    val topics = Set("friend_recommend")
    val kafkaConf = Map[String, Object](
      "zookeeper.connect" -> "localhost:2181",
      "group.id" -> "kafka-spark-streaming",
      "zookeeper.connection.timeout.ms" -> "1000",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "bootstrap.servers" -> "localhost:9092"
    )
    val messages = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaConf)
    )

    val pairs = messages.map{
      msg => friendRecommend.createPair(msg.value)
    }

    val stateDstream = pairs.mapWithState(StateSpec.function(friendRecommend.mappingFunc _))

    // store the result in Cassandra
    stateDstream.saveToCassandra(cassandra_space, friend_recommend_topic, SomeColumns("vertexid", "count", "avg", "recommendations"))

    ssc.checkpoint("/tmp")
    ssc.start()
    ssc.awaitTermination()
  }
}

object friendRecommend {

  def mappingFunc(key: Long, value: Option[String], state: State[(Int, Int, String)]): (Long, Int, Float, String) = {
    (value, state.getOption) match {
      case (Some(v), None) => {
        val new_state = updateState(v, 0, 0, "")
        state.update(new_state)
      }
      case (Some(v), Some((old_count, old_total_hops, old_recommendations))) => {
        val new_state = updateState(v, old_count, old_total_hops, old_recommendations)
        state.update(new_state)
      }
      case _ =>
    }
    val count = state.get._1
    val avg: Float = state.get._2.toFloat / count.toFloat
    val updated_recommendations = state.get._3
    (key, count, avg, updated_recommendations)
  }

  def updateState(values: String, old_count: Int, old_total_hops: Int, old_recommendations: String): (Int, Int, String) = {
    val split = values.split(",")
    var total_hops: Int = old_total_hops
    var sb = new StringBuilder(old_recommendations)
    for (s <- split){
      val recommended_vertex = s.split(" ")(0)
      sb.append(s"$recommended_vertex, ")
      val num_hops = s.split(" ")(1).toInt
      total_hops += num_hops
    }
    val count = old_count + split.size
    (count, total_hops, sb.toString())
  }

  def createPair(s: String): (Long, String) = {
    val split = s.split(":")
    (split(0).toLong, split(1))
  }
}
