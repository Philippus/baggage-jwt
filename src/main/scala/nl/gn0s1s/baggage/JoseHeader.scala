package nl.gn0s1s.baggage

import algorithm._

case class JoseHeader(alg: Algorithm, typ: Option[String], cty: Option[String])

object JoseHeader {
  def apply(alg: Algorithm): JoseHeader = JoseHeader(alg, Some("JWT"), None)
}
