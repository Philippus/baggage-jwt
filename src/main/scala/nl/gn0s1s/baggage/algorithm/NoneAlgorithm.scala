package nl.gn0s1s.baggage
package algorithm

import scala.util._

case object NoneAlgorithm extends Algorithm {
  override def toString = "none"

  def sign(data: String, key: Key): Try[Array[Byte]]                     = Success(Array.emptyByteArray)
  def verify(data: String, key: Key, providedSignature: String): Boolean = providedSignature.isEmpty
}
