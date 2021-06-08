package hw2

import org.apache.spark.rdd.RDD

import scala.collection.SortedSet
import scala.collection.Map

object Main extends App {
  import sqlContext.implicits._

  val k = if (args.length > 0 && args(0).toInt > 1) args(0).toInt else 3  // k: size of frequent item set {a,b,c} k = 3
  val s = if (args.length > 1 && args(1).toInt > 1) args(1).toInt else 10 // s: support
  val c = if (args.length > 2 && args(2).toInt > 1) args(2).toInt * 0.01 else 0.80 // c: confidence fraction to associative rules

  val baskets : RDD[SortedSet[Int]] = sc.textFile("dataset/T10I4D100K.dat")
    .map(line => line.split(" ").map(_.toInt))
    .map(SortedSet(_: _*))

  // Compute the Frequent Items Sets
  // Not necessary to compute "k-1 frequent items" separately, this is just convenience fpr association rule computation later.
  val kMinusOneFrequentItemTable = Apriori.frequentItemSets(baskets, Map[SortedSet[Int],Int](), s, 1, k - 1)
  val kFrequentItemTable =  Apriori.frequentItemSets(baskets, kMinusOneFrequentItemTable, s, k-1, k)

  // val kFrequentItemTable = Apriori.frequentItemSets(baskets, Map[SortedSet[Int],Int](), s, 1, k)
  println()
  println("Frequent Items s = " + s)
  // input to spark DataFrame, presentation purpose only
  val frequentItemsSeq : Seq[(String, Int)] = kFrequentItemTable
    .map {
      case (itemset, count) => Tuple2(itemset.mkString(","), count)
    } (collection.breakOut)

  sqlContext.createDataFrame(frequentItemsSeq)
    .toDF("frequent_items", "count")
    .orderBy($"count".desc)
    .cache
    .show


  // Compute the Association Rules
  println("Association Rules c = " + c)
  val associationRules = AssociationRule.generate(kMinusOneFrequentItemTable, kFrequentItemTable, c, k)

  // input to spark DataFrame, presentation purpose only
  val associationRulesSeq = associationRules.map {
    case (x: SortedSet[Int], y: Int, confidence: Double, xCount: Int, xyCount: Int) =>
      (x.mkString(",") , y, confidence, xCount, xyCount)
  }

  sqlContext.createDataFrame(associationRulesSeq)
    .toDF("X", "Y", "confidence", "X_Count", "XY_Count")
    .orderBy($"confidence".desc)
    .cache
    .show
}
