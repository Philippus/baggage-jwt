package nl.gn0s1s.baggage

import org.scalacheck._
import org.scalacheck.Prop.forAll

import algorithm._

import Generators._

object JoseHeaderSpec extends Properties("JoseHeader") {
  property("contains the supplied parameters") = forAll { (alg: Algorithm, typ: Option[String], cty: Option[String]) =>
    val header = JoseHeader(alg, typ, cty)
    header.alg == alg && header.typ == typ && header.cty == cty
  }

  property("contains only the 'alg' and 'typ' parameters with the correct values when constructed with an algorithm") =
    forAll { alg: Algorithm =>
      val header = JoseHeader(alg)
      header.alg == alg && header.typ.contains("JWT") && header.cty.isEmpty
    }

  property("can be encoded") = forAll { header: JoseHeader =>
    codec.JwtCodec.encodeHeader(header).isSuccess
  }

  property("can be encoded then decoded") = forAll { header: JoseHeader =>
    codec.JwtCodec.encodeHeader(header).flatMap(codec.JwtCodec.decodeHeader).isSuccess
  }

  property("fails decoding if algorithm is not supported") = {
    codec.JwtCodec.decodeHeader("eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9").isFailure
  }

  property("fails decoding if algorithm is not a string") = {
    codec.JwtCodec.decodeHeader("eyJhbGciOjEyMzQ1LCJ0eXAiOiJKV1QifQ").isFailure
  }
}
