package hw1

object Utils {

  def extractFileName(path: String) :String = {
    val pattern = ".+/(.+)\\..+$".r
    val pattern(filename) = path

    filename
  }

  def sha1(token: String) :String = {
    val md = java.security.MessageDigest.getInstance("SHA-1")
    md.digest(token.getBytes("UTF-8")).map("%02x".format(_)).mkString
  }
}
