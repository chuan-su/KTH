import org.apache.spark.SparkContext
import org.apache.spark.sql.{SQLContext, SparkSession}

package object hw2 {

  val spark: SparkSession = SparkSession
    .builder
    .appName("hw2")
    .config("spark master", "local")
    .getOrCreate()

  spark.sparkContext.setLogLevel("ERROR")

  val sqlContext: SQLContext= spark.sqlContext
  val sc: SparkContext = spark.sparkContext
}
