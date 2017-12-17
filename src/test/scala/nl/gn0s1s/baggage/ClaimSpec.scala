package nl.gn0s1s.baggage

import org.scalacheck._
import org.scalacheck.Prop.{ forAll, BooleanOperators }

import claim._

import Generators._
object ClaimSpec extends Properties("Claim") {
  property("generated registered claims are valid") = forAll(genRegisteredClaim) {
    c: Claim =>
      c match {
        case IssuerClaim(_) => c.isValid
        case SubjectClaim(_) => c.isValid
        case AudienceClaim(_) => c.isValid
        case ExpirationTimeClaim(_) => c.isValid
        case NotBeforeClaim(_) => c.isValid
        case IssuedAtClaim(_) => c.isValid
        case JwtIdClaim(_) => c.isValid
        case _ => false
      }
  }

  property("issuer claim accepts stringOrUri") = forAll {
    s: String =>
      !s.contains(':') ==> IssuerClaim(s).isValid
  }

  property("subject claim accepts stringOrUri") = forAll {
    s: String =>
      !s.contains(':') ==> SubjectClaim(s).isValid
  }

  property("audience claim accepts stringOrUri") = forAll {
    s: String =>
      !s.contains(':') ==> AudienceClaim(s).isValid
  }

  property("audience claim accepts several stringOrUri's") = forAll {
    l: List[String] =>
      l.forall(!_.contains(':')) ==> AudienceClaim(l).isValid
  }

  property("expiration time claim accepts numbers >= 0") = forAll {
    l: Long =>
      (l >= 0) ==> ExpirationTimeClaim(l).isValid
  }

  property("not before claim accepts numbers >= 0") = forAll {
    l: Long =>
      (l >= 0) ==> NotBeforeClaim(l).isValid
  }

  property("issued at claim accepts numbers >= 0") = forAll {
    l: Long =>
      (l >= 0) ==> IssuedAtClaim(l).isValid
  }

  property("jwt id claim accepts strings") = forAll {
    s: String =>
      JwtIdClaim(s).isValid
  }

  property("public claims are always valid") = forAll {
    (s: String, a: AnyVal) =>
      PublicClaim(s, a).isValid
  }

  property("private claims are always valid") = forAll {
    (s: String, a: AnyVal) =>
      PrivateClaim(s, a).isValid
  }
}
