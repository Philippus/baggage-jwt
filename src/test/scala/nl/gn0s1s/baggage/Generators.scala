package nl.gn0s1s.baggage

import org.scalacheck.{ Arbitrary, Gen }
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.oneOf

import algorithm._
import claim.Claim

object Generators {
  def genAlgorithm: Gen[Algorithm] = Gen.oneOf(List(NoneAlgorithm, HS256, HS384, HS512))

  implicit val arbitraryAlgorithm: Arbitrary[Algorithm] = Arbitrary(genAlgorithm)

  def genJoseHeader: Gen[JoseHeader] = for {
    alg <- Gen.oneOf(List(NoneAlgorithm, HS256, HS384, HS512))
    typ <- Gen.option[String](Gen.alphaNumStr)
    cty <- Gen.option[String](Gen.alphaNumStr)
  } yield JoseHeader(alg, typ, cty)

  implicit val arbitraryJoseHeader: Arbitrary[JoseHeader] = Arbitrary(genJoseHeader)

  def genRegisteredClaim: Gen[Claim] = for {
    claimName <- oneOf(List("iss", "sub", "aud", "exp", "nbf", "iat", "jti"))
    claimValue <- if (List("iss", "sub", "aud", "jti").contains(claimName)) Gen.alphaNumStr else Gen.posNum[Int]
  } yield Claim(claimName, claimValue)

  def genKey: Gen[Key] = for {
    value <- arbitrary[Array[Byte]]
  } yield Key(value)

  implicit val arbitraryKey: Arbitrary[Key] = Arbitrary(genKey)
}
