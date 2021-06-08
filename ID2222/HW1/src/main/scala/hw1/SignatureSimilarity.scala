package hw1

import org.apache.spark.sql.{ DataFrame }
import org.apache.spark.sql.functions._

object SignatureSimilarity {
  import spark.implicits._

  def compute(signatureMatrix: DataFrame) :DataFrame = {

    val docNames : Seq[String] = signatureMatrix.columns.filterNot(_.equals("idx")).toSeq

    val similarityMatrix : DataFrame = sqlContext.createDataFrame(docNames.map(Tuple1(_))).toDF("doc")

    val total = signatureMatrix.count()

    val calculateSimilarity = (doc1 : String, doc2 : String) => {
      signatureMatrix
        .filter(col(doc1).===(col(doc2)))
        .count()
        .toDouble
        ./(total)
    }

    val func = udf(calculateSimilarity)

    docNames.foldLeft(similarityMatrix) {
      case (result, docName) => result.withColumn(docName, func($"doc", lit(docName)))
    }
  }
}
