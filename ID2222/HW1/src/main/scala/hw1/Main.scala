package hw1

object Main extends App {

  val docs = (1 to 10).map(i => "dataset/email"+i+".txt") ++ Seq("dataset/plagiarism1.txt", "dataset/plagiarism2.txt")

  val k = if (args.length > 0 && args(0).toInt > 1) args(0).toInt else 4
  val characteristicMatrix = CharacteristicMatrix.create(k, docs: _*).cache

  println()
  println("Characteristic Matrix k = " + k)
  characteristicMatrix.show()

  println("Jaccard Similarity Matrix")
  JaccardSimilarity.compute(characteristicMatrix).show()

  val n = if (args.length > 1 && args(1).toInt > 1) args(1).toInt else 100 // n signatures
  val signatureMatrix = SignatureMatrix.create(characteristicMatrix, n).cache

  println("Signature Matrix")
  signatureMatrix.show()

  println("Signature Similarity Matrix n = " + n)
  SignatureSimilarity.compute(signatureMatrix).show()


  val b = if (args.length > 2 && args(2).toInt > 1) args(2).toInt else 25
  val r = if (args.length > 3 && args(3).toInt > 1) args(3).toInt else 4

  println("Candidate Signature Matrix b = " + b +" r = " + r )
  val candidates = LSH.selectCandidates(signatureMatrix, b, r).cache
  candidates.show()

  println("Candidate Signature Similarity Matrix")
  SignatureSimilarity.compute(candidates).show()

  println("Shingle   k = " + k)
  println("Signature n = " + n)
  println("Band      b = " + b)
  println("Row       r = " + r)
}