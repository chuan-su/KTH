package hw2

import scala.collection.SortedSet
import scala.collection.Map
import scala.collection.Seq

object AssociationRule {

  /**
    * Computs the association rules with confidence above
    * @param kMinusOneFrequentItems computed frequent item set for item size of size k-1. {a,b} if k = 3
    * @param kFrequentItems computed frequent item set for item size of k {a,b,c} if k = 3
    * @param minConfidence
    * @param k
    * @tparam T
    * @return
    */
  def generate[T](kMinusOneFrequentItems: Map[SortedSet[T], Int],
                  kFrequentItems: Map[SortedSet[T], Int],
                  minConfidence: Double,
                  k: Int): Seq[(SortedSet[T], T, Double, Int, Int)] = {

    kFrequentItems
      .map { case (itemSet, count) => Tuple2(itemSet, count)}(collection.breakOut)
      .flatMap { case (itemSet, count) =>
        itemSet.subsets(k - 1) // get k-1 frequent items of {a,b,c}, which is  {a,b}, {a,c}, {b,c}
          .foldLeft(Seq[(SortedSet[T], T, Double, Int, Int)]()) {
            case (result, x) => { // x = {a,b}
              // X -> Y
              // count = count of (X U Y)
              // xCount = count of X
              val xCount = kMinusOneFrequentItems(x)
              val confidenceRate = count.toDouble / xCount

              if (confidenceRate >= minConfidence) {
                val y = (itemSet -- x).head // value of y which is: {a,b,c} intersect {a,b} => {c}
                val confidence = BigDecimal(confidenceRate).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble

                result :+ (x, y, confidence, xCount, count) // append rule to result set
              } else{
                result
              }
            }
          }
      }
  }
}
