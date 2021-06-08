package hw3

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
import scala.io.Source
import org.apache.spark.graphx._

/*
  dataset links:

  http://konect.uni-koblenz.de/networks/moreno_names
  http://konect.uni-koblenz.de/networks/ucidata-gama
  */
object Main extends App {

  //spark configurations
  val conf = new SparkConf().setAppName("Simple Application").setMaster("local[*]")

  //interacts with spark
  val sc = new SparkContext(conf)

  val sampleSize = if (args.length > 0 && args(0).toInt > 6) args(0).toInt else 100 //M
  val algorithm = if (args.length > 1) args(1) else "base"

  //val filename = "dataset/bible/out.moreno_names_names" // sample size can be 3000
  val filename = "dataset/gama/out.ucidata-gama"

  println()
  println("Sample Size: " + sampleSize)
  println("Triest Algorithm: " + algorithm)
  
  val stat = new TriestSampleStat(sampleSize)

  for (line <- Source.fromFile(filename)
    .getLines
    .filter(line => !line.contains("%"))
  ) {
    val arr = line.split("\\s+").map(_.toInt)
    val edge = Edge(arr(0), arr(1), 1)

    if (algorithm.equals("impr")) {
      TriestImpr.estimateTriangleCount(edge, stat)
    } else {
      Triest.estimateTriangleCount(edge, stat)
    }
  }

  println("Triangle count: "+stat.triangleEstimate())

}
