name := "baggage-jwt"
organization := "nl.gn0s1s"
version := "0.3.1"
startYear := Some(2017)
homepage := Some(url("https://github.com/philippus/baggage-jwt"))
licenses += ("BSD 3-Clause", url("http://opensource.org/licenses/BSD-3-Clause"))

crossScalaVersions := List("2.12.10", "2.13.0")
scalaVersion := crossScalaVersions.value.head

bintrayOrganization := Some("gn0s1s")
bintrayRepository := "releases"

useJCenter := true

libraryDependencies ++= Seq(
  "co.blocke" %% "scalajack" % "6.0.4",
  "org.scalacheck" %% "scalacheck" % "1.14.2" % Test
)

pomExtra :=
  <scm>
    <url>git@github.com:Philippus/baggage-jwt.git</url>
    <connection>scm:git@github.com:Philippus/baggage-jwt.git</connection>
  </scm>
  <developers>
    <developer>
      <id>philippus</id>
      <name>Philippus Baalman</name>
      <url>https://github.com/philippus</url>
    </developer>
  </developers>
