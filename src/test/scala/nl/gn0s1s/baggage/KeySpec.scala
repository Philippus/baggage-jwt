package nl.gn0s1s.baggage

import org.scalacheck._

object KeySpec extends Properties("Key") {
  property("provides an empty key") = {
    Key.emptyKey.toString == ""
  }

  property("can be generated from array bytes - example one") = {
    val key = Key(BigInt("fefeffff", 16).toByteArray.drop(1))
    key.toString == "/v7//w=="
  }

  property("can be generated from array bytes - example two") = {
    val key = Key(BigInt("ffffffff", 16).toByteArray.drop(1))
    key.toString == "/////w=="
  }
}
