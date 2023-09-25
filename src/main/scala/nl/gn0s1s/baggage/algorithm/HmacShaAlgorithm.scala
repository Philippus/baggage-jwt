package nl.gn0s1s.baggage
package algorithm

import java.nio.charset.StandardCharsets.UTF_8
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import scala.util._

import codec.JwtCodec

abstract class HmacShaAlgorithm(algorithmString: String) extends Algorithm {
  def sign(data: String, key: Key): Try[Array[Byte]] = Try {
    val secretKey = new SecretKeySpec(key.value, algorithmString)
    val mac       = Mac.getInstance(algorithmString)
    mac.init(secretKey)
    mac.doFinal(data.getBytes(UTF_8))
  }

  def verify(data: String, key: Key, providedSignature: String): Boolean = {
    for {
      signature        <- sign(data, key)
      encodedSignature <- JwtCodec.encodeSignature(signature)
    } yield MessageDigest.isEqual(encodedSignature.getBytes, providedSignature.getBytes) // prevent timing attacks
  }.getOrElse(false)
}

case object HS256 extends HmacShaAlgorithm("HmacSHA256")
case object HS384 extends HmacShaAlgorithm("HmacSHA384")
case object HS512 extends HmacShaAlgorithm("HmacSHA512")
