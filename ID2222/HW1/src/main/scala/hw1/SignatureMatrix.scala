package hw1

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._

import scala.util.Random

object SignatureMatrix {
  import spark.implicits._

  def create(characteristicMatrix : DataFrame, nSignatures: Int) : DataFrame = {

    val docNames :Array[String] = characteristicMatrix.columns.filterNot(_.equals("idx"))

    //  construct random hash functions to simulate row permutation
    val nRows = characteristicMatrix.count().intValue()
    val hashFunctions : Seq[Int => Int] = randCoefficients(nRows, nSignatures)
      .map { case (a, b, prime) => (x: Int) => { x.*(a).+(b).%(prime) } }

    // rowNumbers we are going to "permutate" on.
    val rowNumbers : DataFrame = characteristicMatrix.select(col("idx"))
    // nSignatures hash functions we are going to use to permutate on each document characteristic matrix
    val permutation : DataFrame = hashFunctions
      .map(udf(_))
      .zipWithIndex
      .foldLeft(rowNumbers) { case (result, (f, i) ) => result.withColumn("hash"+i, f($"idx")) }

    // original characteristic matrix for each document, but only with columns that equal to "1" to calculate minHash
    val docDataFrames : Array[DataFrame] = docNames
      .map(docName => characteristicMatrix.select(col("idx"), col(docName))
      .filter(col(docName) === 1))

    // signatures for each document
    val hashFuncNames : Array[String] = permutation.columns.filterNot(_.equals("idx")) // column names
    val signatures: Array[Seq[Int]] = docDataFrames
      .map(docDataFrame =>
        // Join original characteristic matrix with permutation
        docDataFrame.join(permutation, docDataFrame.col("idx") === permutation.col("idx"))
          .select(hashFuncNames.map(min): _*) // select MinHash for each permutation
          .first
          .toSeq
          .map{ case signature: Int => signature})

    // start building signature matrix, first column is all the indexes of hash function names
    val hashFunctionIndexes = hashFuncNames.zipWithIndex.map { case (_, index) => Tuple1(index)}
    val signatureMatrix = sqlContext.createDataFrame(hashFunctionIndexes).toDF("idx")

    // Now append each document signatures to the matrix
    val extractSignature = (docIndex: Int, signatureIndex: Int) => { signatures(docIndex)(signatureIndex) }
    val func = udf(extractSignature)

    docNames.zipWithIndex
      .foldLeft(signatureMatrix) {
        case (result, (doc, docIndex)) => result.withColumn(doc, func(lit(docIndex), lit($"idx")))
      }
  }

  def randCoefficients(nRow: Int, n: Int): Seq[(Int, Int, Int)] = {
    val rnd = new Random(99999)
    (0 until n)
      .map(_ => {
        val prime = nRow + 1
        val a = rnd.nextInt(prime)
        val b = rnd.nextInt(prime)
        (a, b, prime)
      })
  }
}
