package nl.gn0s1s.baggage

import scala.util._

import algorithm._
import claim.ClaimsSet._
import codec.JwtCodec._

case class JsonWebToken(encodedHeader: String, encodedPayload: String, encodedSignature: String) {
  override def toString = s"$encodedHeader.$encodedPayload.$encodedSignature"

  lazy val claimsOption: Option[ClaimsSet] = this.decode.map(_._2).toOption

  def decode: Try[(JoseHeader, ClaimsSet, String)] = {
    for {
      header <- decodeHeader(encodedHeader)
      claims <- decodePayload(encodedPayload)
    } yield (header, claims, encodedSignature)
  }

  def validate(alg: Algorithm, secretKey: Key): Boolean = JsonWebToken.validate(this.toString, alg, secretKey)
}

object JsonWebToken {
  def apply(header: JoseHeader, claims: ClaimsSet, secretKey: Key): Try[JsonWebToken] = {
    for {
      encodedHeader <- encodeHeader(header)
      encodedPayload <- encodePayload(claims)
      signature <- header.alg.sign(s"$encodedHeader.$encodedPayload", secretKey)
      encodedSignature <- encodeSignature(signature)
    } yield JsonWebToken(encodedHeader, encodedPayload, encodedSignature)
  }

  def apply(alg: Algorithm, claims: ClaimsSet, secretKey: Key): Try[JsonWebToken] = {
    JsonWebToken(JoseHeader(alg), claims, secretKey)
  }

  def apply(jwtString: String): Try[JsonWebToken] = {
    val parts = jwtString.split('.')

    if ((parts.length == 2 && jwtString.last == '.') || parts.length >= 3) {
      val encodedSignature = if (parts.isDefinedAt(2)) parts(2) else ""

      for {
        _ <- decodeHeader(parts(0))
        _ <- decodePayload(parts(1))
      } yield JsonWebToken(parts(0), parts(1), encodedSignature)
    } else
      Failure(new IllegalArgumentException("jwt should contain at least two parts"))
  }

  def validate(jwtString: String, alg: Algorithm, secretKey: Key): Boolean = {
    for {
      jwt <- JsonWebToken(jwtString)
      header <- decodeHeader(jwt.encodedHeader)
    } yield header.alg == alg && alg.verify(s"${jwt.encodedHeader}.${jwt.encodedPayload}", secretKey, jwt.encodedSignature)
  }.getOrElse(false)
}
