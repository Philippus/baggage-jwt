package nl.gn0s1s.baggage

import scala.util.{Failure, Success}

import org.scalacheck._
import org.scalacheck.Prop.{forAll, propBoolean}

import algorithm._
import claim._

import Generators._

object JsonWebTokenSpec extends Properties("JsonWebToken") {
  property("can generate a jwt from a header and a key") = forAll { (header: JoseHeader, key: Key) =>
    (key.value.nonEmpty || header.alg == NoneAlgorithm) ==> {
      val jwt = JsonWebToken(header, Set.empty[Claim], key)
      jwt.isSuccess
    }
  }

  property("can generate a jwt from an algorithm and a key") = forAll { (alg: Algorithm, key: Key) =>
    (key.value.nonEmpty || alg == NoneAlgorithm) ==> {
      val jwt = JsonWebToken(alg, Set.empty[Claim], key)
      jwt.isSuccess
    }
  }

  property("encoding and then validating succeeds if key is not empty or algorithm = \"none\"") = forAll {
    (header: JoseHeader, key: Key) =>
      (key.value.nonEmpty || header.alg == NoneAlgorithm) ==> {
        val jwt = JsonWebToken(header, Set.empty[Claim], key)
        jwt.isSuccess && JsonWebToken.validate(jwt.get.toString, header.alg, key)
      }
  }

  property("jwt should contain at least two parts") = forAll { (alg: Algorithm, key: Key) =>
    !JsonWebToken.validate("a.", alg, key)
  }

  property("apply only works for at least two parts") = {
    JsonWebToken("a.") match {
      case Failure(_: IllegalArgumentException) => true
      case _ => false
    }
  }

  // https://tools.ietf.org/html/rfc7519#section-3.1
  property("decodes examples in the wild - example in rfc 7519") = {
    val exampleFromRfc2 =
      """eyJ0eXAiOiJKV1QiLA0KICJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJqb2UiLA0KICJleHAiOjEzMDA4MTkzODAsDQogImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ.dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"""
    JsonWebToken(exampleFromRfc2).map(_.decode).get match {
      case Success((header, claims, _)) =>
        header == JoseHeader(HS256, Some("JWT"), None) && claims == Set(
          IssuerClaim("joe"),
          ExpirationTimeClaim(1300819380),
          PrivateClaim("http://example.com/is_root", true)
        )
      case Failure(_) => false
    }
  }

  // https://tools.ietf.org/html/rfc7519#section-6.1
  property("decodes examples in the wild - example in rfc 7519") = {
    val exampleFromRfc =
      """eyJhbGciOiJub25lIn0.eyJpc3MiOiJqb2UiLA0KICJleHAiOjEzMDA4MTkzODAsDQogImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ."""
    JsonWebToken(exampleFromRfc).map(_.decode).get match {
      case Success((header, claims, _)) =>
        header == JoseHeader(NoneAlgorithm, None, None) && claims == Set(
          IssuerClaim("joe"),
          ExpirationTimeClaim(1300819380),
          PrivateClaim("http://example.com/is_root", true)
        )
      case Failure(_) => false
    }
  }

  // https://developer.atlassian.com/static/connect/docs/latest/concepts/understanding-jwt.html
  property("decodes examples in the wild - example from atlassian") = {
    val exampleFromAtlassian =
      """eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjEzODY4OTkxMzEsImlzcyI6ImppcmE6MTU0ODk1OTUiLCJxc2giOiI4MDYzZmY0Y2ExZTQxZGY3YmM5MGM4YWI2ZDBmNjIwN2Q0OTFjZjZkYWQ3YzY2ZWE3OTdiNDYxNGI3MTkyMmU5IiwiaWF0IjoxMzg2ODk4OTUxfQ.uKqU9dTB6gKwG6jQCuXYAiMNdfNRw98Hw_IWuA5MaMo"""
    JsonWebToken(exampleFromAtlassian).map(_.decode).get match {
      case Success((header, claims, _)) =>
        header == JoseHeader(HS256, Some("JWT"), None) && claims == Set(
          IssuerClaim("jira:15489595"),
          ExpirationTimeClaim(1386899131),
          PrivateClaim("qsh", "8063ff4ca1e41df7bc90c8ab6d0f6207d491cf6dad7c66ea797b4614b71922e9"),
          IssuedAtClaim(1386898951)
        )
      case Failure(_) => false
    }
  }

  // https://jwt.io/
  property("decodes examples in the wild - example from jwt.io") = {
    val exampleFromJwtIo =
      """eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ"""
    JsonWebToken(exampleFromJwtIo).map(_.decode).get match {
      case Success((header, claims, _)) =>
        header == JoseHeader(HS256, Some("JWT"), None) && claims == Set(
          SubjectClaim("1234567890"),
          PublicClaim("name", "John Doe"),
          PrivateClaim("admin", true)
        )
      case Failure(_) => false
    }
  }

  // https://github.com/Philippus/authentikat-jwt/blob/7ad7a0efac6a4d95cec20671e96248948d840b6a/README.md#jwt---a-claims-based-authentication
  property("decodes examples in the wild - example from authentikat-jwt") = {
    val exampleFromAuthentikat =
      """eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJIZXkiOiJmb28ifQ.fTW9f2w5okSpa7u64d6laQQbpBdgoTFvIPcx5gi70R8"""
    JsonWebToken(exampleFromAuthentikat).map(_.decode).get match {
      case Success((header, claims, _)) =>
        header == JoseHeader(HS256, Some("JWT"), None) && claims == Set(PrivateClaim("Hey", "foo"))
      case Failure(_) => false
    }
  }
}
