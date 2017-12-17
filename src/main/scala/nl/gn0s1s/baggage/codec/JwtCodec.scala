package nl.gn0s1s.baggage
package codec

import java.nio.charset.StandardCharsets.UTF_8
import java.util.Base64
import scala.util.Try

import co.blocke.scalajack.{ Reader, ScalaJack, TokenType, Writer }
import co.blocke.scalajack.typeadapter.BasicTypeAdapter

import algorithm.Algorithm
import claim.ClaimsSet._

object JwtCodec {
  private val sj = ScalaJack[String]().withAdapters(AlgorithmAdapter)

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

object AlgorithmAdapter extends BasicTypeAdapter[Algorithm] {
  override def read(reader: Reader): Algorithm = {
    reader.peek match {
      case TokenType.String =>
        Algorithm(reader.readString).getOrElse(throw new Exception("invalid algorithm specified"))
      case _ =>
        throw new Exception("invalid algorithm specified")
    }
  }
  override def write(value: Algorithm, writer: Writer): Unit =
    writer.writeString(value.toString)
}
