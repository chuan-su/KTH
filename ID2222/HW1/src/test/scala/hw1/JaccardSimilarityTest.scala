package hw1

import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.FunSuite

import scala.io.Source
import org.apache.spark.sql.{Column, DataFrame, Row}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import spark.implicits._

class JaccardSimilarityTest extends FunSuite{

  test("compare lists") {

    //spark configurations
    val conf = new SparkConf().setAppName("Simple Application").setMaster("local[*]")

    //interacts with spark
    val sc = new SparkContext(conf)

    //Given
    val df = (Seq(0,0,1,0), Seq(1,0,1,1), Seq(1,0,1,0)).zipped.toList.toDF("Field1", "Field2", "field3")

    //When
    val similarity = JaccardSimilarity.compute(df)

    //Then
    val col1 = similarity.select("Field1").rdd.map(r => r(0)).collect()
    val col2 = similarity.select("Field2").rdd.map(r => r(0)).collect()
    val col3 = similarity.select("Field3").rdd.map(r => r(0)).collect()

    assert(col1(0)==1.0)
    assert(col1(1)==0.33)
    assert(col1(2)==0.5)

    assert(col2(1)==1.0)
    assert(col2(2)== 0.67)

    assert(col3(2)==1.0)
  }
}