package nl.gn0s1s.baggage
package codec

import java.nio.charset.StandardCharsets.UTF_8
import java.util.Base64
import scala.util.Try

import co.blocke.scalajack._
import co.blocke.scalajack.model.{ Parser, Stringish, TypeAdapter, Writer }

import algorithm.Algorithm
import claim.ClaimsSet._

object JwtCodec {
  private val sj = ScalaJack().withAdapters(AlgorithmAdapter)

  def encodeHeader(header: JoseHeader): Try[String] = {
    Try(new String(Base64.getUrlEncoder.withoutPadding.encode(sj.render(header).getBytes(UTF_8))))
  }

  def decodeHeader(encodedHeader: String): Try[JoseHeader] = {
    Try(sj.read[JoseHeader](new String(Base64.getDecoder.decode(encodedHeader), UTF_8)))
  }

  def encodePayload(claims: ClaimsSet): Try[String] = {
    Try(new String(Base64.getUrlEncoder.withoutPadding.encode(sj.render(claimsSetToMap(claims)).getBytes)))
  }

  def decodePayload(encodedPayload: String): Try[ClaimsSet] = {
    Try(claimsMapToSet(sj.read[ClaimsMap](new String(Base64.getDecoder.decode(encodedPayload)))))
  }

  def encodeSignature(signature: Array[Byte]): Try[String] = {
    Try(new String(Base64.getUrlEncoder.withoutPadding.encode(signature)))
  }
}

object AlgorithmAdapter extends TypeAdapter.===[Algorithm] with Stringish {
  def read(parser: Parser): Algorithm =
    parser.expectString() match {
      case s: String =>
        Algorithm(s).getOrElse(throw new Exception("invalid algorithm specified"))
      case _ =>
        throw new Exception("invalid algorithm specified")
    }

  def write[WIRE](alg: Algorithm, writer: Writer[WIRE], out: scala.collection.mutable.Builder[WIRE, WIRE]): Unit =
    writer.writeString(alg.toString, out)
}
