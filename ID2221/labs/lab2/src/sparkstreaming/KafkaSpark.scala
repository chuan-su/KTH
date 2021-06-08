package sparkstreaming

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

object KafkaSpark {
  def main(args: Array[String]) {
    // connect to Cassandra and make a keyspace and table as explained in the document
    val cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
    val session = cluster.connect()
    session.execute("CREATE KEYSPACE IF NOT EXISTS avg_space WITH REPLICATION = { 'class': 'SimpleStrategy', 'replication_factor' : 1 };")
    session.execute("CREATE TABLE IF NOT EXISTS avg_space.avg (word text PRIMARY KEY, count float);")

    // make a connection to Kafka and read (key, value) pairs from it
    val conf = new SparkConf().setMaster("local[2]").setAppName("KeyAverage")
    val ssc = new StreamingContext(conf, Seconds(1))
    ssc.sparkContext.setLogLevel("ERROR")
    val topics = Set("avg")
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

    def createPair(s: String): (String, Int) = {
      val split = s.split(",")
      (split(0), split(1).toInt)
    }

    val pairs = messages.map(msg => createPair(msg.value))

    // measure the average value for each key in a stateful manner
    def mappingFunc(key: String, value: Option[Int], state: State[(Int, Int)]): (String, Float) = {
      (value, state.getOption) match {
        case (Some(v), None) => state.update((v, 1))
        case (Some(v), Some((sum, size))) => state.update((sum + v, size + 1))
        case _ =>
      }
      val avg: Float = state.get._1.toFloat / state.get._2.toFloat
      (key, avg)
    }
    val stateDstream = pairs.mapWithState(StateSpec.function(mappingFunc _))

    // store the result in Cassandra
    stateDstream.saveToCassandra("avg_space", "avg", SomeColumns("word", "count"))

    ssc.checkpoint("/tmp")
    ssc.start()
    ssc.awaitTermination()
  }
}
