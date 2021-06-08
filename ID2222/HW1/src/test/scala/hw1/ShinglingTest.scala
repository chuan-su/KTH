package hw1

import org.scalatest.FunSuite

class ShinglingTest extends FunSuite {

  test("k-shingles of 'abcde'"){
    // Given
    val text: String = "abcde"
    val k: Int = 2

    // When
    val shingels : Set[String] = Shingling.kShingles(text, k)

    // Then
    val formattedText = text.toLowerCase()

    assert(shingels.contains("ab"))
    assert(shingels.contains("bc"))
    assert(shingels.contains("cd"))
    assert(shingels.contains("de"))
  }
}
