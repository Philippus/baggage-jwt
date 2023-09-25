package nl.gn0s1s.baggage
package claim

object ClaimsSet {
  type ClaimValue = Any
  type ClaimsMap  = Map[String, Any]
  type ClaimsSet  = Set[Claim]

  def claimsMapToSet(claims: ClaimsMap): Set[Claim] = {
    claims.map(x => Claim(x._1, x._2)).toSet
  }

  def claimsSetToMap(set: Set[Claim]): ClaimsMap = {
    set.map(claim => claim.name -> claim.value).toMap
  }
}
