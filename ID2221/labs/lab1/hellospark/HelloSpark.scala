import org.apache.spark.sql.SparkSession

object HelloSpark {
    def main(args: Array[String]) {
	val logFile = "data/hamlet.txt"
	val spark = SparkSession.builder.appName("Hello Spark").master("local[2]").getOrCreate()
	val sc = spark.sparkContext
	val logData = sc.textFile(logFile).cache()
	val wordCounts = logData.flatMap(line => line.split(" ")).map(word => (word, 1)).reduceByKey((a, b) => a + b)
	wordCounts.foreach(println(_))
	spark.stop()
    }
}
