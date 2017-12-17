package nl.gn0s1s.baggage

import java.util.Base64

case class Key(value: Array[Byte]) {
  override def toString = Base64.getEncoder.encodeToString(value)
}

object Key {
  lazy val emptyKey = Key(Array.emptyByteArray)
}
