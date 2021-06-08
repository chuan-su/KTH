import org.apache.spark.sql.{SQLContext, SparkSession}

package object hw1 {

  val spark: SparkSession = SparkSession
    .builder
    .appName("hw1")
    .config("spark master", "local")
    .getOrCreate()

  spark.sparkContext.setLogLevel("ERROR")

  val sqlContext: SQLContext= spark.sqlContext
}
