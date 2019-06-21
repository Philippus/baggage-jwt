# Baggage JWT

[![Build Status](https://travis-ci.org/Philippus/baggage-jwt.svg?branch=master)](https://travis-ci.org/Philippus/baggage-jwt)
[![codecov](https://codecov.io/gh/Philippus/baggage-jwt/branch/master/graph/badge.svg)](https://codecov.io/gh/Philippus/baggage-jwt)
![Current Version](https://img.shields.io/badge/version-0.3.1-brightgreen.svg?style=flat "0.3.1")
[![License](https://img.shields.io/badge/License-BSD%203--Clause-blue.svg?style=flat "BSD 3-Clause")](LICENSE.md)

Baggage JWT is an implementation of [RFC 7519](https://tools.ietf.org/html/rfc7519) in Scala that tries to follow the
specification closely.

## Installation
Baggage JWT is published for Scala 2.12 and 2.13. Add the following to your `build.sbt`:

```
resolvers += Resolver.bintrayRepo("gn0s1s", "releases")

libraryDependencies += "nl.gn0s1s" %% "baggage-jwt" % "0.3.1"
```

## JWT

## Example usage

### Creating a token
A JsonWebToken can be created through three methods:
- `JsonWebToken(header, claims, secretKey)` with the parameters: 
  - header: JoseHeader - the header to use
  - claims: ClaimsSet - the claims to encode in the jwt
  - secretKey: Key - the key used to sign the jwt

- `JsonWebToken(alg, claims, secretKey)`: in this case a default header will be generated for the supplied algorithm `alg`

- `JsonWebToken(jwtString)`: here the jwtString is an already encoded JWT.

```scala
import nl.gn0s1s.baggage._
import nl.gn0s1s.baggage.algorithm._
import nl.gn0s1s.baggage.claim._
JsonWebToken(header = JoseHeader(HS256, Some("JWT"), None), Set(SubjectClaim("1234567890"), PublicClaim("name", "John Doe"), PrivateClaim("admin", true)), Key("secret".getBytes))
// res0: scala.util.Try[nl.gn0s1s.baggage.JsonWebToken] = Success(eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ)

JsonWebToken(alg = HS256, Set(SubjectClaim("1234567890"), PublicClaim("name", "John Doe"), PrivateClaim("admin", true)), Key("secret".getBytes))
// res1: scala.util.Try[nl.gn0s1s.baggage.JsonWebToken] = Success(eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ)

JsonWebToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ")
// res2: scala.util.Try[nl.gn0s1s.baggage.JsonWebToken] = Success(eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ)
```
### Header
A header can be generated using `JoseHeader(alg, typ, cty)` with the parameters:
- `alg`: algorithm header parameter to use in the jwt
- `typ`: type header parameter, usually contains "JWT"
- `cty`: content type header parameter, usually can stay empty

An extra method `JoseHeader(alg)` is available where only the `alg` parameter needs to be provided, a `typ` of "JWT" and
no `cty` will be added as defaults.

```scala
JoseHeader(HS256, Some("JWT"), None)
// res3: nl.gn0s1s.baggage.JoseHeader = JoseHeader(HS256,Some(JWT),None)
JoseHeader(NoneAlgorithm)
// res4: nl.gn0s1s.baggage.JoseHeader = JoseHeader(none,Some(JWT),None)
```
### Claims
For the registered claim names: `iss`, `sub`, `aud`, `exp`, `nbf`, `iat`, and `jti` there are implementations available.
Baggage JWT is strict about the allowed values inside registered claims.
 
### Algorithms
The following algorithms are supported:
- HS256
- HS384
- HS512
- None

### Keys

### Validating a token
To validate a token the method `JsonWebToken.validate` can be used, it requires:
- `jwtString`: the jwt being validated (as a string)
- `alg`: the algorithm to validate against
- `secretKey`: the key to validate against

```scala
val jwt = res0.get
JsonWebToken.validate(jwt.toString, HS256, Key("secret".getBytes))
// res5: Boolean = true

```
### Processing claims
Most likely you want to write your own claims processor, but an example of a claims processor
(see: `ClaimsProcessor.process`) is included which requires the following parameters:
- `claims`: the claims being processed.
- `reqClaimNames`: the names of claims that are required to be there.
- `expectedClaims`: the claims that are expected to match exactly, note that the `aud` claim is processed differently than the rest.
- `clockSkew`: the clock skew to take into account for the `exp` and `nbf` claims.

The result is a `Success` containing the claims, or a `Failure` containing an appropriate exception.

```scala
val claims = jwt.decode.get._2
ClaimsProcessor.process(claims, Set("sub", "name", "admin"), Set(PrivateClaim("admin", true)), java.time.Duration.ZERO)
// res6: scala.util.Try[nl.gn0s1s.baggage.claim.ClaimsSet.ClaimsSet] = Success(Set(SubjectClaim(1234567890), PublicClaim(name,John Doe), PrivateClaim(admin,true)))
ClaimsProcessor.process(claims, Set("sub", "name", "admin", "iat"), Set(PrivateClaim("admin", true)), java.time.Duration.ZERO)
// res7: scala.util.Try[nl.gn0s1s.baggage.claim.ClaimsSet.ClaimsSet] = Failure(java.lang.IllegalArgumentException: Not all required claim names present in claims set)
```
## References
 - [RFC 7519](https://tools.ietf.org/html/rfc7519)
 - https://en.wikipedia.org/wiki/JSON_Web_Token
 - [jwt.io](https://jwt.io/)
 - [authentikat-jwt](https://github.com/jasongoodwin/authentikat-jwt) - Another JWT Scala implementation which inspired
 this version
- [public claims](https://www.iana.org/assignments/jwt/jwt.xhtml)

## License
The code is available under the [BSD 3-Clause](LICENSE.md).
