package hw1

import org.apache.spark.sql.{Column, DataFrame, Row}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import spark.implicits._

object LSH {

  def selectCandidates(signatureMatrix: DataFrame, b: Int, r: Int): DataFrame = {
    // build sub bands
    val bands : Seq[DataFrame] = (0 until b * r).grouped(r).toVector
      .map {
        case head +: tail  => signatureMatrix.filter($"idx".between(head, tail.last)).toDF().drop("idx")
      }

    // Hash each columns in each band into buckets
    val buckets : Seq[Array[(String,Int)]] = bands.map(band =>
      band.columns
        .map { band.select(_).collect().toList.mkString }
        .map(Utils.sha1) // Use sha1 or md5 would be sufficient here, as hashing is not important in LSH theory.
        .zipWithIndex
        .map {
          case (hash, index) => (hash, index) // we need the column/document index in oder to get them back later.
        })

    // Candidate Set with document indexes
    val candidateIndexes: Set[Int] = buckets.foldLeft(Set[Int]()) {
      case (result, bucket) => {
        // find out the ones that are hashed into same bucket
        // collect the column indexes into candidate set
        val a = bucket.groupBy(_._1).mapValues(a => a.map(_._2)).collect { case(hash, indexes) if indexes.length > 1 => indexes }.flatten.toSet
        result ++ a
      }
    }

    // Map candidate index into actual column names
    val docNames = bands(0).columns
    val candidateColumns = candidateIndexes.map(docNames(_)).toArray.map(col)

    // Return the candidate matrix
    signatureMatrix.select(candidateColumns: _*).toDF()
  }
}