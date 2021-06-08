package hw2

import org.apache.spark.rdd.RDD

import scala.collection.SortedSet
import scala.collection.Map

object Apriori {

  /**
   * Compute frequent item table with support above `support`
   * @param baskets baskets or transactions
   * @param frequentItemsTable frequent items table computed from previous iteraction
   * @param k starts from 1 up to `size` indicatiing the size of frequent item set. {a,b,c}, size = 3
   */
  def frequentItemSets[T](baskets: RDD[SortedSet[T]],
                          frequentItemsTable: Map[SortedSet[T], Int], // FrequentItemsTable created from prev iteration
                          support: Int,
                          k: Int,
                          size: Int) : Map[SortedSet[T], Int] = {
    if (k > size) {
      return frequentItemsTable
    }

    val nFrequentItemsTable: Map[SortedSet[T], Int]  = baskets
      .flatMap { basket =>
        val candidates = basket.subsets(k)
        if (frequentItemsTable.isEmpty) {
          candidates
        } else {
          candidates.filter(existsInFrequentItemTable(_, frequentItemsTable))
        }
      }
      .map(itemSet => (itemSet, 1))
      .reduceByKey(_ + _)
      .filter { case (_, count) => count >= support }
      .collectAsMap

    frequentItemSets(baskets, nFrequentItemsTable, support, k + 1, size)
  }

  /**
   *  Determine if an item set exists in the FrequentItemTable computed from previous iteration
   */
  private def existsInFrequentItemTable[T](candidate : SortedSet[T],
                                           frequentItemsTable: Map[SortedSet[T], Int]): Boolean = {
    // previous FrequentItemsTable contains only item sets of size : (k-1)
    // if any of (k-1) combination of k-itemset does not exists in previous FrequentItemsTable
    // then k-itemset are definitely not frequent item set
    candidate.subsets(candidate.size - 1)
        .forall(frequentItemsTable.contains)
  }
}
