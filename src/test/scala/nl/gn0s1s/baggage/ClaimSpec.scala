package nl.gn0s1s.baggage

import java.time.{ Duration, LocalDateTime, ZoneOffset }
import scala.util.Failure

import org.scalacheck._
import org.scalacheck.Prop.{ forAll, propBoolean }

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

  property("audience claim rejects non-stringOrUri's") = forAll {
    i: Int =>
      !AudienceClaim(i).isValid
  }

  property("audience claim rejects several non-stringOrUri's") = forAll {
    l: List[Int] =>
      l.nonEmpty ==> !AudienceClaim(l).isValid
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

  property("processor fails if not all required claim names present in claims set") = {
    val claims = Set.empty[Claim]
    ClaimsProcessor.process(claims, Set("iat"), Set.empty[Claim], Duration.ZERO) match {
      case Failure(x: IllegalArgumentException) => x.getMessage == "Not all required claim names present in claims set"
      case _ => false
    }
  }

  property("processor fails if not all expected claim names match claims in set") = {
    val claims = Set[Claim](IssuerClaim("gn0s2s"))
    ClaimsProcessor.process(claims, Set.empty[String], Set(IssuerClaim("gn0s1s")), Duration.ZERO) match {
      case Failure(x: IllegalArgumentException) => x.getMessage == "Not all expected claims match claims in set"
      case _ => false
    }
  }

  property("processor fails if audience claim does not match audience claim in set - list") = {
    val claims = Set[Claim](AudienceClaim(List("public", "private")))
    ClaimsProcessor.process(claims, Set.empty[String], Set(AudienceClaim("exclusive")), Duration.ZERO) match {
      case Failure(x: IllegalArgumentException) => x.getMessage == "Audience claim does not match audience claim in set"
      case _ => false
    }
  }

  property("processor succeeds if audience claim does not match audience claim in set - list") = {
    val claims = Set[Claim](AudienceClaim(List("public", "private")))
    ClaimsProcessor.process(claims, Set.empty[String], Set(AudienceClaim("public")), Duration.ZERO).isSuccess
  }

  property("processor fails if audience claim does not match audience claim in set") = {
    val claims = Set[Claim](AudienceClaim("private"))
    ClaimsProcessor.process(claims, Set.empty[String], Set(AudienceClaim("public")), Duration.ZERO) match {
      case Failure(x: IllegalArgumentException) => x.getMessage == "Audience claim does not match audience claim in set"
      case _ => false
    }
  }

  property("processor succeeds if audience claim matches audience claim in set") = {
    val claims = Set[Claim](AudienceClaim("public"))
    ClaimsProcessor.process(claims, Set.empty[String], Set(AudienceClaim("public")), Duration.ZERO).isSuccess
  }

  property("processor fails if token has expired") = {
    val current = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
    val claims = Set[Claim](ExpirationTimeClaim(current - 60))
    ClaimsProcessor.process(claims, Set.empty[String], Set.empty[Claim], Duration.ZERO) match {
      case Failure(x: IllegalArgumentException) => x.getMessage == "Token has expired"
      case _ => false
    }
  }

  property("processor takes into account clockSkew for processing expiration time claim") = {
    val current = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
    val claims = Set[Claim](ExpirationTimeClaim(current - 60))
    ClaimsProcessor.process(claims, Set.empty[String], Set.empty[Claim], Duration.ofSeconds(65)).isSuccess
  }

  property("processor fails if token not valid yet") = {
    val current = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
    val claims = Set[Claim](NotBeforeClaim(current + 60))
    ClaimsProcessor.process(claims, Set.empty[String], Set.empty[Claim], Duration.ZERO) match {
      case Failure(x: IllegalArgumentException) => x.getMessage == "Token not valid yet"
      case _ => false
    }
  }

  property("processor takes into account clockSkew for processing not before claim") = {
    val current = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
    val claims = Set[Claim](NotBeforeClaim(current + 60))
    ClaimsProcessor.process(claims, Set.empty[String], Set.empty[Claim], Duration.ofSeconds(65)).isSuccess
  }

  property("processor can process examples in the wild - example in rfc 7519") = {
    val exampleFromRfc2 = """eyJ0eXAiOiJKV1QiLA0KICJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJqb2UiLA0KICJleHAiOjEzMDA4MTkzODAsDQogImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ.dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"""
    val claims = JsonWebToken(exampleFromRfc2).map(_.decode).get.get._2
    val current = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
    ClaimsProcessor.process(claims, Set("iss", "exp", "http://example.com/is_root"), Set(IssuerClaim("joe"), PrivateClaim("http://example.com/is_root", true)), Duration.ofSeconds(current - 1300819380 + 60)).isSuccess
  }

  property("processor can process examples in the wild - example from Atlassian") = {
    val exampleFromAtlassian = """eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjEzODY4OTkxMzEsImlzcyI6ImppcmE6MTU0ODk1OTUiLCJxc2giOiI4MDYzZmY0Y2ExZTQxZGY3YmM5MGM4YWI2ZDBmNjIwN2Q0OTFjZjZkYWQ3YzY2ZWE3OTdiNDYxNGI3MTkyMmU5IiwiaWF0IjoxMzg2ODk4OTUxfQ.uKqU9dTB6gKwG6jQCuXYAiMNdfNRw98Hw_IWuA5MaMo"""
    val claims = JsonWebToken(exampleFromAtlassian).map(_.decode).get.get._2
    val current = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
    ClaimsProcessor.process(claims, Set("iss", "exp", "qsh", "iat"), Set(IssuerClaim("jira:15489595"), PrivateClaim("qsh", "8063ff4ca1e41df7bc90c8ab6d0f6207d491cf6dad7c66ea797b4614b71922e9")), Duration.ofSeconds(current - 1386899131 + 60)).isSuccess
  }

  property("processor can process examples in the wild - example from jwt.io") = {
    val exampleFromJwtIo = """eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ"""
    val claims = JsonWebToken(exampleFromJwtIo).map(_.decode).get.get._2
    ClaimsProcessor.process(claims, Set("sub", "name", "admin"), Set(SubjectClaim("1234567890"), PublicClaim("name", "John Doe"), PrivateClaim("admin", true)), Duration.ZERO).isSuccess
  }

  property("processor can process examples in the wild - example from authentikat-jwt") = {
    val exampleFromAuthentikat = """eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJIZXkiOiJmb28ifQ.fTW9f2w5okSpa7u64d6laQQbpBdgoTFvIPcx5gi70R8"""
    val claims = JsonWebToken(exampleFromAuthentikat).map(_.decode).get.get._2
    ClaimsProcessor.process(claims, Set("Hey"), Set(PrivateClaim("Hey", "foo")), Duration.ZERO).isSuccess
  }
}
