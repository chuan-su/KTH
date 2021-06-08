package hw1

import org.apache.spark.sql.{ DataFrame }
import org.apache.spark.sql.functions._

object JaccardSimilarity {
  import spark.implicits._

  def compute(characteristicMatrix: DataFrame) :DataFrame = {

    val docNames : Seq[String] = characteristicMatrix.columns.filterNot(_.equals("idx")).toSeq

    val similarityMatrix : DataFrame = sqlContext.createDataFrame(docNames.map(Tuple1(_))).toDF("doc")

    val calculateSimilarity = (doc1 : String, doc2 : String) => {

      val total = characteristicMatrix
        .filter(col(doc1) === 1 || col(doc2) === 1)
        .count()

      val intersection = characteristicMatrix
        .filter(col(doc1).===(1) &&  col(doc2).===(1))
        .count()

      val similarity = intersection.toDouble./(total)

      BigDecimal(similarity).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
    }

    val func = udf(calculateSimilarity)

    docNames.foldLeft(similarityMatrix) {
      case (result, docName) => result.withColumn(docName, func($"doc", lit(docName)))
    }
  }
}
