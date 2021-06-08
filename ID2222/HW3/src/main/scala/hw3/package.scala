import org.apache.spark.SparkContext
import org.apache.spark.sql.{SQLContext, SparkSession}

package object hw3 {

  val spark: SparkSession = SparkSession
    .builder
    .appName("hw3")
    .config("spark master", "local")
    .getOrCreate()

  spark.sparkContext.setLogLevel("ERROR")

  val sqlContext: SQLContext= spark.sqlContext
  val sc: SparkContext = spark.sparkContext
}