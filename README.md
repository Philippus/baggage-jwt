# Baggage JWT

[![Build Status](https://travis-ci.org/Philippus/baggage-jwt.svg?branch=master)](https://travis-ci.org/Philippus/baggage-jwt)
[![codecov](https://codecov.io/gh/Philippus/baggage-jwt/branch/master/graph/badge.svg)](https://codecov.io/gh/Philippus/baggage-jwt)
![Current Version](https://img.shields.io/badge/version-0.1.0-brightgreen.svg?style=flat "0.1.0")
[![License](https://img.shields.io/badge/License-BSD%203--Clause-blue.svg?style=flat "BSD 3-Clause")](LICENSE.md)

Baggage JWT is an implementation of [RFC 7519](https://tools.ietf.org/html/rfc7519) in Scala that tries to follow the
specification closely.

## Installation
Baggage JWT is published for Scala 2.11 and 2.12, but requires Java 8. Add the following to your `build.sbt`:

```
resolvers += Resolver.bintrayRepo("gn0s1s", "releases")

libraryDependencies += "nl.gn0s1s" %% "baggage-jwt" % "0.1.0"
```

## JWT

*TODO*:
- write README
- processing claims

## Example usage

### Creating a token

### Header

### Claims

### Algorithms

The following algorithms are supported:
- HS256 / HS384 / HS512
- None

### Keys

### Validating a token

### Processing claims

## References
 - [RFC 7519](https://tools.ietf.org/html/rfc7519)
 - https://en.wikipedia.org/wiki/JSON_Web_Token
 - [jwt.io](https://jwt.io/)
 - [authentikat-jwt](https://github.com/jasongoodwin/authentikat-jwt) - Another JWT Scala implementation which inspired
 this version
- [public claims](https://www.iana.org/assignments/jwt/jwt.xhtml)

## License
The code is available under the [BSD 3-Clause](LICENSE.md).
