package nl.gn0s1s.baggage
package claim

import java.time._
import scala.util._

import ClaimsSet.ClaimsSet

object ClaimsProcessor {
  def process(claims: ClaimsSet, reqClaimNames: Set[String], expectedClaims: ClaimsSet, clockSkew: Duration): Try[ClaimsSet] = {
    // required claim names should be in the supplied claims
    val containsAllReqClaims = reqClaimNames -- claims.map(_.name) == Set.empty

    // expected claims should be in the supplied claims (audience claim is handled separately)
    val expectedClaimsArePresent = expectedClaims.filterNot(_.name == "aud") -- claims == Set.empty

    // check if expected audience claim value is present in supplied set of claims
    val checkAudienceClaim = if (expectedClaims.exists(_.name == "aud")) {
      val audExpClaim = expectedClaims.filter(_.name == "aud").head
      claims.filter(_.name == "aud").forall(_.value match {
        case l: List[Any] => l.contains(audExpClaim.value)
        case x => x == audExpClaim.value
      })
    } else true

    val current = LocalDateTime.now()

    // if expiration time claim is present it should be checked
    val checkExpirationTimeClaim = if (claims.exists(_.name == "exp")) {
      val expirationTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(claims.filter(_.name == "exp").head.value match { case l: Long => l }), ZoneOffset.UTC)
      current.isBefore(expirationTime.plus(clockSkew))
    } else true

    // if not before claim is present it should be checked
    val checkNotBeforeClaim = if (claims.exists(_.name == "nbf")) {
      val notBeforeTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(claims.filter(_.name == "nbf").head.value match { case l: Long => l }), ZoneOffset.UTC)
      !current.isBefore(notBeforeTime.minus(clockSkew))
    } else true

    for {
      claims <- if (containsAllReqClaims) Success(claims) else Failure(new IllegalArgumentException("Not all required claim names present in claims set"))
      claims <- if (expectedClaimsArePresent) Success(claims) else Failure(new IllegalArgumentException("Not all expected claims match claims in set"))
      claims <- if (checkAudienceClaim) Success(claims) else Failure(new IllegalArgumentException("Audience claim does not match audience claim in set"))
      claims <- if (checkExpirationTimeClaim) Success(claims) else Failure(new IllegalArgumentException("Token has expired"))
      claims <- if (checkNotBeforeClaim) Success(claims) else Failure(new IllegalArgumentException("Token not valid yet"))
    } yield claims
  }
}
