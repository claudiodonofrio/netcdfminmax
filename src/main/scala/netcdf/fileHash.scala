package netcdf

import java.security.{DigestInputStream, MessageDigest}
import java.io.{File, FileInputStream}
import java.nio.file.Path


class fileHash (){
  // Compute a hash of a file
  // The output of this function should match the output of running "md5 -q <file>"
  def computeHash (path: Path): String = {
    val buffer = new Array[Byte] (4096)
    // val md5 = MessageDigest.getInstance ("MD5")
    val md5 = MessageDigest.getInstance("SHA-256")

    val dis = new DigestInputStream (new FileInputStream (path.toFile), md5)
    try {
      while (dis.read (buffer) != - 1) {
      }
    } finally {
      dis.close ()
    }
    md5.digest.map ("%02x".format (_) ).mkString
}
}