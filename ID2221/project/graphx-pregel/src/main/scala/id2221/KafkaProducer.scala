package id2221

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

object KafkaProducer {

  def send(producerRecord: ProducerRecord[String, String]): Unit = {
    //kafka producer
//     val brokers = "192.168.1.234:32775,192.168.1.234:32777"
    val brokers = "localhost:9092"
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    val producer = new KafkaProducer[String, String](props)
    producer.send(producerRecord)
    producer.close()
  }
}
