package hw1

object Shingling {

  def kShingles(str : String, k : Int) : Set[String] = {

    val allowedChars = ('a' to 'z').toSet ++ (0 to 9).map(_.toString).toSet

    str.trim
      .replaceAll(" +", " ") // convert multiple spaces to a single space
      .map(Character.toLowerCase)
      .filter(allowedChars.contains(_)) // remove punctuations
      .sliding(k)
      .toSet
  }

  /**
    * Computes the hash of a shingle
    * @param shingle
    * @return
    */
  private def hash(shingle:String):Int = {
    util.hashing.MurmurHash3.stringHash(shingle)
  }

  /**
    * Computes hashes on list of shingles, returns them
    * in ascending order
    * @param shingles
    * @return
    */
  def computeHashes(shingles:List[String]):List[Int] = {
    shingles.map(hash).sortWith(_ < _)
  }
}
