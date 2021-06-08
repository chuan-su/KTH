package id2221
import java.util.HashMap
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{StringDeserializer, DoubleDeserializer}
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.storage.StorageLevel
import java.util.{Date, Properties}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, ProducerConfig}
import scala.util.Random

import org.apache.spark.sql.cassandra._
import com.datastax.spark.connector._
import com.datastax.driver.core.{Session, Cluster, Host, Metadata}
import com.datastax.spark.connector.streaming._
import scala.collection.mutable.Map

object CommunityConsumer {
  def main(args: Array[String]) {
    // connect to Cassandra and make a keyspace and table as explained in the document
    val cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
    val session = cluster.connect()
    val cassandra_space = "id2221_space"
    val community_topic = "community"
    session.execute(s"CREATE KEYSPACE IF NOT EXISTS $cassandra_space WITH REPLICATION = { 'class': 'SimpleStrategy', 'replication_factor' : 1 };")
    session.execute(s"CREATE TABLE IF NOT EXISTS $cassandra_space.$community_topic (vertex_id varchar PRIMARY KEY, peers varchar, count int, max int);")

    // make a connection to Kafka and read (key, value) pairs from it
    val conf = new SparkConf().setMaster("local[4]").setAppName("CommunityConsumer")
    val ssc = new StreamingContext(conf, Seconds(1))
    ssc.sparkContext.setLogLevel("ERROR")
    val topics = Set(community_topic)
    val kafkaConf = Map[String, Object](
//      "zookeeper.connect" -> "192.168.1.234:2181",
      "zookeeper.connect" -> "localhost:2181",
      "group.id" -> "kafka-spark-streaming",
      "zookeeper.connection.timeout.ms" -> "1000",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "bootstrap.servers" -> "localhost:9092"
//      "bootstrap.servers" -> "192.168.1.234:32775,192.168.1.234:32777"
    )
    val messages = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaConf)
    )

    def max(key: String, value: Option[String], state: State[(Int, Int)]): (String, String, Int, Int) = {
      (value, state.getOption()) match {
        case (Some(v), None) => state.update((1, v.split(",").length))
        case (Some(v), Some((count, max))) => {
          val vc = v.split(",").length
          state.update((count +1, if (vc > max) vc else max))
        }
      }
      (key, value.getOrElse(""), state.get._1, state.get._2)
    }

    val pairs = messages.map(record => (record.key(), record.value()))
    val stateStream = pairs.mapWithState(StateSpec.function(max _))
    stateStream.saveToCassandra(cassandra_space, community_topic, SomeColumns("vertex_id", "peers", "count", "max"))

    stateStream.print()
    ssc.checkpoint("/tmp")
    ssc.start()
    ssc.awaitTermination()
  }
}