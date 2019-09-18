package nl.gn0s1s.baggage

import org.scalacheck._
import org.scalacheck.Prop.{ forAll, propBoolean }

import algorithm._
import codec.JwtCodec.encodeSignature
import Generators._

object AlgorithmSpec extends Properties("Algorithm") {
  property("returns None for not supported algorithms") = forAll {
    (a: String) =>
      !List(NoneAlgorithm, HS256, HS384, HS512).map(_.toString).contains(a) ==> {
        Algorithm(a) match {
          case Some(_) => false
          case None => true
        }
      }
  }

  property("signing fails if keys is empty") = forAll {
    (alg: Algorithm, data: String) =>
      (alg != NoneAlgorithm) ==>
        alg.sign(data, Key.emptyKey).isFailure
  }

  property("signing and then verifying succeeds if key is not empty") = forAll {
    (alg: Algorithm, data: String, key: Key) =>
      key.value.nonEmpty ==>
        alg.verify(data, key, encodeSignature(alg.sign(data, key).get).get)
  }

  property("signing and then verifying succeeds if algorithm = \"none\" (even if key is empty)") = forAll {
    (alg: Algorithm, data: String, key: Key) =>
      (alg == NoneAlgorithm) ==>
        alg.verify(data, key, encodeSignature(alg.sign(data, key).get).get)
  }

  property("verifying fails if signature is invalid") = forAll {
    (alg: Algorithm, data: String, key: Key, signature: String) =>
      (key.value.nonEmpty && alg != NoneAlgorithm && signature != encodeSignature(alg.sign(data, key).get).get) ==>
        !alg.verify(data, key, signature)
  }

  propertyWithSeed("verifying fails if key is invalid", None) = forAll {
    (alg: Algorithm, data: String, key: Key, verificationKey: Key) =>
      (!key.value.sameElements(verificationKey.value) && key.value.nonEmpty && verificationKey.value.nonEmpty && alg != NoneAlgorithm) ==>
        !alg.verify(data, verificationKey, encodeSignature(alg.sign(data, key).get).get)
  }

  property("verifying fails if signing fails") = forAll {
    (alg: Algorithm, data: String, key: Key) =>
      (key.value.nonEmpty && alg != NoneAlgorithm) ==>
        !alg.verify(data, Key.emptyKey, encodeSignature(alg.sign(data, key).get).get)
  }

  property("verifying fails if signature is invalid for algorithm = \"none\"") = forAll {
    (alg: Algorithm, data: String, key: Key, signature: String) =>
      (signature.nonEmpty && alg == NoneAlgorithm) ==>
        !alg.verify(data, key, signature)
  }
}
