package nl.gn0s1s.baggage
package claim

import java.time._
import scala.util._

import ClaimsSet.ClaimsSet

object ClaimsProcessor {
  def checkRequiredClaims(claims: ClaimsSet, reqClaimNames: Set[String]): Try[ClaimsSet] = {
    if (reqClaimNames -- claims.map(_.name) == Set.empty)
      Success(claims)
    else
      Failure(new IllegalArgumentException("Not all required claim names present in claims set"))
  }

  // expected claims should be in the supplied claims
  def checkExpectedClaims(claims: ClaimsSet, expectedClaims: ClaimsSet): Try[ClaimsSet] = {
    if (expectedClaims -- claims == Set.empty)
      Success(claims)
    else
      Failure(new IllegalArgumentException("Not all expected claims match claims in set"))
  }

  // check if expected audience claim value is present in supplied set of claims
  def checkAudienceClaim(claims: ClaimsSet, audienceClaim: Option[Claim]): Try[ClaimsSet] = {
    val checkAudienceClaim = claims.filter(_.name == "aud").forall(_.value match {
      case l: List[Any] => l.exists(x => audienceClaim.exists(_.value == x))
      case x => audienceClaim.exists(_.value == x)
    })

    if (checkAudienceClaim)
      Success(claims)
    else
      Failure(new IllegalArgumentException("Audience claim does not match audience claim in set"))
  }

  def checkExpirationTimeClaim(claims: ClaimsSet, clockSkew: Duration): Try[ClaimsSet] = {
    val current = LocalDateTime.now()

    val checkExpirationTimeClaim = claims.filter(_.name == "exp").forall(_.value match {
      case l: Long =>
        val expirationTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(l), ZoneOffset.UTC)
        current.isBefore(expirationTime.plus(clockSkew))
    })

    if (checkExpirationTimeClaim)
      Success(claims)
    else
      Failure(new IllegalArgumentException("Token has expired"))
  }

  // if not before claim is present it should be checked
  def checkNotBeforeClaim(claims: ClaimsSet, clockSkew: Duration): Try[ClaimsSet] = {
    val current = LocalDateTime.now()

    val checkNotBeforeClaim = claims.filter(_.name == "nbf").forall(_.value match {
      case l: Long =>
        val notBeforeTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(l), ZoneOffset.UTC)
        !current.isBefore(notBeforeTime.minus(clockSkew))
    })

    if (checkNotBeforeClaim)
      Success(claims)
    else
      Failure(new IllegalArgumentException("Token not valid yet"))
  }

  def process(claims: ClaimsSet, reqClaimNames: Set[String], expectedClaims: ClaimsSet, clockSkew: Duration): Try[ClaimsSet] = {
    for {
      claims <- checkRequiredClaims(claims, reqClaimNames)
      claims <- checkExpectedClaims(claims, expectedClaims.filterNot(_.name == "aud"))
      claims <- checkAudienceClaim(claims, expectedClaims.find(_.name == "aud"))
      claims <- checkExpirationTimeClaim(claims, clockSkew)
      claims <- checkNotBeforeClaim(claims, clockSkew)
    } yield claims
  }
}
