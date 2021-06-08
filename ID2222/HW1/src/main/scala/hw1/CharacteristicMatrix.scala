package hw1

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._

object CharacteristicMatrix {

  import spark.implicits._

  def create(k: Int, docs: String*) :DataFrame = {

    val docMap: Seq[(String, Set[String])] = docs
      .map { path => (Utils.extractFileName(path), spark.read.textFile(path)) }
      .map { case (docName, dataset) => (docName, dataset.reduce(_ + " " + _)) }
      .map { case (docName, text) => (docName, Shingling.kShingles(text, k)) }

    val universalShingleSet: Set[String] = docMap.map(_._2).reduce { (set1, set2) => set1 ++ set2 }

    // Build Characteristic Matrix
    var df: DataFrame = sqlContext.createDataFrame(universalShingleSet.toSeq.zipWithIndex)
      .toDF("shingle", "idx")

    var characteristicMatrix = docMap.foldLeft(df) {
      case (matrix, (docname, shingleSet)) =>
        matrix.withColumn(docname, when($"shingle".isin(shingleSet.toSeq: _*), 1).otherwise(0))
    }
    // Drop the actual shingle column since we will use its index from now
    characteristicMatrix.drop("shingle")
  }
}
