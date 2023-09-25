package nl.gn0s1s.baggage
package algorithm

import scala.util._

abstract class Algorithm {
  def sign(data: String, key: Key): Try[Array[Byte]]
  def verify(data: String, key: Key, providedSignature: String): Boolean
}

object Algorithm {
  def apply(a: String): Option[Algorithm] = a match {
    case "none"  => Some(NoneAlgorithm)
    case "HS256" => Some(HS256)
    case "HS384" => Some(HS384)
    case "HS512" => Some(HS512)
    case _       => None
  }
}
