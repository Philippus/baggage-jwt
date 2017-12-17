package nl.gn0s1s.baggage
package claim

import java.net.URI
import scala.util.Try

abstract class Claim(name: String, value: Any) {
  def getName: String = name
  def getValue: Any = value
  def isValid: Boolean
}

trait StringOrUri {
  def value: String

  def isValid: Boolean = !value.contains(':') || Try(new URI(value)).isSuccess
}

trait NumericDate {
  def value: Long

  def isValid: Boolean = value >= 0
}

abstract class RegisteredClaim(name: String, value: Any) extends Claim(name, value)

case class IssuerClaim(value: String) extends RegisteredClaim("iss", value) with StringOrUri // stringOrURI

case class SubjectClaim(value: String) extends RegisteredClaim("sub", value) with StringOrUri // stringOrURI

case class AudienceClaim(value: Any) extends RegisteredClaim("aud", value) { // stringOrUri or array of stringOrUri
  def isValid: Boolean = {
    def stringOrUri(value: String): Boolean = !value.exists(_ == ':') || Try(new URI(value)).isSuccess

    value match {
      case s: String => stringOrUri(s)
      case l: List[Any] => l.forall({ case (s: String) => stringOrUri(s) })
      case _ => false
    }
  }
}

case class ExpirationTimeClaim(value: Long) extends RegisteredClaim("exp", value) with NumericDate // number containing NumericDate

case class NotBeforeClaim(value: Long) extends RegisteredClaim("nbf", value) with NumericDate // number containing NumericDate

case class IssuedAtClaim(value: Long) extends RegisteredClaim("iat", value) with NumericDate // number containing NumericDate

case class JwtIdClaim(value: String) extends RegisteredClaim("jti", value) { // string
  def isValid = true
}

case class PublicClaim(name: String, value: Any) extends Claim(name, value) {
  def isValid = true
}

case class PrivateClaim(name: String, value: Any) extends Claim(name, value) {
  def isValid = true
}

object Claim {
  val RegisteredClaimNames = List("iss", "sub", "aud", "exp", "nbf", "iat", "jti")
  val PublicClaimNames = List(
    "name",
    "given_name",
    "family_name",
    "middle_name",
    "nickname",
    "preferred_username",
    "profile",
    "picture",
    "website",
    "email",
    "email_verified",
    "gender",
    "birthdate",
    "zoneinfo",
    "locale",
    "phone_number",
    "phone_number_verified",
    "address",
    "updated_at",
    "azp",
    "nonce",
    "auth_time",
    "at_hash",
    "c_hash",
    "acr",
    "amr",
    "sub_jwk",
    "cnf",
    "sip_from_tag",
    "sip_date",
    "sip_callid",
    "sip_cseq_num",
    "sip_via_branch",
    "orig",
    "dest",
    "mky")

  def apply(name: String, value: Any): Claim = {
    (name, value) match {
      case ("iss", s: String) => IssuerClaim(s)
      case ("sub", s: String) => SubjectClaim(s)
      case ("aud", s: String) => AudienceClaim(s)
      case ("aud", l: List[Any]) => AudienceClaim(l)
      case ("exp", i: Int) => ExpirationTimeClaim(i.toLong)
      case ("nbf", i: Int) => NotBeforeClaim(i.toLong)
      case ("iat", i: Int) => IssuedAtClaim(i.toLong)
      case ("jti", s: String) => JwtIdClaim(s)
      case (name, v) if RegisteredClaimNames.contains(name) => throw new IllegalArgumentException(s"wrong type ${v.getClass} for value of ${name}")
      case (name, v) if PublicClaimNames.contains(name) => PublicClaim(name, v)
      case (name, v) => PrivateClaim(name, v)
    }
  }
}
